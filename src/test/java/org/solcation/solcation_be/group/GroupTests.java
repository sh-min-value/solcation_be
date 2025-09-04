package org.solcation.solcation_be.group;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.solcation.solcation_be.domain.group.dto.GroupListDTO;
import org.solcation.solcation_be.repository.GroupMemberRepository;
import org.solcation.solcation_be.repository.GroupRepository;
import org.solcation.solcation_be.util.s3.S3Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

@Slf4j
@SpringBootTest
public class GroupTests {
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Test
    public void testGetGroupList() {
        String userId = "admin";

        List<Object[]> result = groupRepository.getGroupListWithSearch(userId, "");

        log.info("getGroupList result:{}", result.get(1)[6].toString());
    }

    @Test
    public void getGroupInfo() {
        Object result = groupRepository.getGroupInfoByGroupPk(10);
        log.info("getGroupInfo result:{}", result);
        for (Object obj : (Object[])result) {
            System.out.println(obj.toString());
        }
    }
}
