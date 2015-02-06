package com.poolingpeople.deployer.boundary;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

public class ContainersInfoReaderTest {

    ContainersInfoReader cut;

    @Before
    public void setUp() throws Exception {
        cut = new ContainersInfoReader();
    }

    @Test
    public void testGetContainers() throws Exception {
        Collection<ContainerInfo> containers =
                cut.getContainers(this.getClass().getClassLoader().getResourceAsStream("containersList.json"));

        ;
        assertThat(containers.stream().map(c -> c.getId()).collect(Collectors.toList())
                , containsInAnyOrder(
                "d4c1376327379239fc0f7c0d121d86abe4bf37e01bd66480690ff381993b021c",
                "b13538a6cc73f691ea011143a77dd910a02840934045f9bee403ae3ad0eb7196",
                "4f09ae44b139f58433b94d45ab775e58fbfa1292b9265fc56b1ab0e8017f5183",
                "4def0169b8c15126b7493f965ef79d7af9b7fdce54f324173a8f2c352ac5eca8",
                "14b46dc14e6301e51b0c489c2ee79d3aeabe35748999cc7bab204532a92da9b1"
        ));

//        System.out.println(containers);
        assertThat(containers.size(), Is.is(5));
    }
}