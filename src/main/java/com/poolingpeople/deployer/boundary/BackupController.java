package com.poolingpeople.deployer.boundary;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.poolingpeople.deployer.dockerapi.boundary.BackupInfo;
import com.poolingpeople.deployer.dockerapi.boundary.DockerApi;
import com.poolingpeople.deployer.scenario.boundary.AWSCredentials;
import org.apache.commons.compress.utils.IOUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.faces.model.CollectionDataModel;
import javax.faces.model.DataModel;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by hendrik on 20.04.15.
 */


@Singleton  // only one per application
@Startup
@Named
public class BackupController {

    @Inject
    DockerApi dockerApi;

    @Resource
    private TimerService timerService;

    // TODO: wrap them for different server
    DataModel<BackupInfo> model;
    Collection<BackupInfo> list = new ArrayList<>();
    Collection<String> containersToBeEnabled;

    Logger logger = Logger.getLogger(getClass().getName());


    public BackupController() {

    }

    @PostConstruct
    @SuppressWarnings("unchecked")
    private void startup() {
        System.out.println("STARTED");

        // Register timer auto-backup
        logger.log(Level.INFO, "ProgrammaticalTimerEJB initialized");
        //ScheduleExpression scheduleExpression = new ScheduleExpression().second("*/10").minute("*").hour("*");
        ScheduleExpression scheduleExpression = new ScheduleExpression().hour("16");
        timerService.createCalendarTimer(scheduleExpression, new TimerConfig("passed message " + new Date(), false));

        // Try to load old settings
        getContainers();
        try {
            FileInputStream fis = new FileInputStream(getStoreFile());
            ObjectInputStream ois = new ObjectInputStream(fis);
            Collection<String> enabledContainers = (Collection<String>) ois.readObject();
            list.stream().forEach(elem -> {
                if(enabledContainers.contains(elem.getContainer().getId())) // container should be backup
                    elem.setBackup(true);
            });
            ois.close();
        } catch (ClassNotFoundException | IOException e) {
            logger.log(Level.WARNING, "No list of backup-container found");
            //e.printStackTrace();
        }
    }


    @PreDestroy
    private void shutdown() {
        System.out.println("DESTROY");

        // Save container backup list
        try {

            list.stream().forEach(elem -> System.out.println(elem.getContainer().getId() + " -> " + elem.isBackup()));

            Collection<String> enabledContainers = list.stream()
                    .filter(BackupInfo::isBackup)
                    .map(elem -> elem.getContainer().getId())
                    .collect(Collectors.toList());

            FileOutputStream fileOutputStream = new FileOutputStream(getStoreFile());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(enabledContainers);
            objectOutputStream.close();
            System.out.println("SAVED SETTINGS");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private File getStoreFile() {
        // check folder to exists
        File folder = new File("./backupconfig");
        if(!folder.exists()) {
            folder.mkdirs();
        }
        return new File("./backupconfig/ContainerListForBackup.ser");
    }

    // For more info see: http://www.hascode.com/2012/06/task-scheduling-in-java-ee-6-on-glassfish-using-the-timer-service/#Programmatic_Timer_Creation
    @Timeout
    public void handleTimer(final Timer timer) {
        logger.info("timer received - contained message is: " + timer.getInfo());
        //doBackup();
    }

    /**
     * get all containers with neo4j databases.
     * all containers returned can be enabled for backup
     * @return
     *          all containers with neo4j databases
     */
    public DataModel<BackupInfo> getContainers() {
        list = dockerApi.listContainers("http://localhost:5555").stream()
                .filter(c -> c.getNames().stream().anyMatch(n -> n.toLowerCase().contains("neo"))) // only get databases
                .map(container -> {
                    BackupInfo bi = new BackupInfo(container);
                    // check if this container is already known
                    Optional<BackupInfo> elem = list.stream().filter(knownContainer -> knownContainer.getContainer().getId().equals(container.getId())).findAny();
                    if (elem.isPresent()) { // copy data from already known container
                        bi.setBackup(elem.get().isBackup());
                    }
                    return bi;
                })
                .collect(Collectors.toCollection(ArrayList::new));

        model = new CollectionDataModel<>(list);
        return model;
    }

    /**
     * enables the backup for the current container.
     * This container will be saved when running a backup
     */
    public void enableBackup() {
        BackupInfo current = model.getRowData();
        current.setBackup(true);
    }


    /**
     * disables the backup for the current container.
     * This container will be not longer be saved when running a backup
     */
    public void disableBackup() {
        BackupInfo current = model.getRowData();
        current.setBackup(false);
    }

    /**
     * run a backup on all selected containers
     */
    public void doBackup() {
        logger.fine("Starting a backup");

        // Backup all selected databases
        list.stream().filter(BackupInfo::isBackup).map(BackupInfo::getContainer).forEach(containerInfo -> {

            byte[] data;
            try {
                InputStream dbInputStream = dockerApi.copyFiles(containerInfo.getId(), "/var/lib/neo4j/data/graph.db/");
                data = IOUtils.toByteArray(dbInputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            AmazonS3 s3client = new AmazonS3Client(new AWSCredentials());

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(data.length);


            // Save chronological backup
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String filename = "neo4j-db-backups/" + df.format(new Date()) + "_" + containerInfo.getDomainLink() + ".tar";
            s3client.putObject(
                    new PutObjectRequest(
                            "poolingpeople",
                            filename,
                            new ByteArrayInputStream(data),
                            objectMetadata
                    ));

            // Save latest backup
            s3client.putObject(
                    new PutObjectRequest(
                            "poolingpeople",
                            "neo4j-db-dev/" + containerInfo.getDomainLink() + ".tar", // TODO: Change to "neo4j-db/"
                            new ByteArrayInputStream(data),
                            objectMetadata
                    ));
        });
    }

}
