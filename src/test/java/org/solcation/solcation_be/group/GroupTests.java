package org.solcation.solcation_be.group;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.solcation.solcation_be.util.s3.S3Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class GroupTests {
    @Autowired
    private S3Utils s3Utils;

}
