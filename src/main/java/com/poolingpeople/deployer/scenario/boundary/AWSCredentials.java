package com.poolingpeople.deployer.scenario.boundary;

import com.amazonaws.auth.BasicAWSCredentials;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by hendrik on 15.04.15.
 */
public class AWSCredentials extends BasicAWSCredentials{

    public AWSCredentials() {
        super(getKey("aws-access-key"), getKey("aws-secret-key"));
    }

    public static String getKey(String name) {

        String key = System.getenv(name);

        if( key != null) return key;

        InputStream kStream = AWSCredentials.class.getClassLoader().getResourceAsStream(name);

        if( kStream == null ){
            throw new RuntimeException("aws keys not found");
        }

        return streamToString(kStream);

    }

    private static String streamToString(InputStream in)  {

        try {

            StringBuilder out = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            for (String line = br.readLine(); line != null; line = br.readLine()) {
                out.append(line);
            }

            br.close();
            return out.toString();

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
