package org.radarcns.management.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.radarcns.management.ManagementPortalTestApp;
import org.radarcns.management.domain.enumeration.ProjectStatus;
import org.radarcns.management.service.dto.AttributeMapDTO;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.dto.SubjectDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by nivethika on 31-8-17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
@Transactional
public class RedcapIntegrationWorkFlowOnServiceLevelTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SubjectService subjectService;

    @Test
    public void testRedcapIntegrationWorkFlowOnServiceLevel() {
        final String externalProjectUrl = "MyUrl";
        final String externalProjectId = "MyId";
        final String projectLocation = "London";
        final String workPackage = "MDD";
        final String phase = "1";


        ProjectDTO projectDto = new ProjectDTO();
        projectDto.setDescription("Test Project");
        projectDto.setLocation(projectLocation);
        projectDto.setProjectName("test radar");
        projectDto.setProjectStatus(ProjectStatus.PLANNING);
        Set<AttributeMapDTO> projectMetaData = new HashSet<>();
        projectMetaData.add(new AttributeMapDTO(ProjectDTO.EXTERNAL_PROJECT_URL_KEY,
                externalProjectUrl));
        projectMetaData.add(new AttributeMapDTO(ProjectDTO.EXTERNAL_PROJECT_ID_KEY,
                externalProjectId));
        projectMetaData.add(new AttributeMapDTO(ProjectDTO.PHASE_KEY, phase));
        projectMetaData.add(new AttributeMapDTO(ProjectDTO.WORK_PACKAGE_KEY, workPackage));
        projectDto.setAttributes(projectMetaData);

        // manually save
        ProjectDTO saved = projectService.save(projectDto);
        Long storedProjectId = saved.getId();
        assertThat(storedProjectId > 0);

        // Use ROLE_EXTERNAL_ERF_INTEGRATOR authority in your oauth2 client config
        // GET api/projects/{storedProjectId}
        ProjectDTO retrievedById = projectService.findOne(storedProjectId);
        assertEquals(retrievedById.getId(), storedProjectId);

        // retrieve required details
        // location is part of project property
        final String locationRetrieved = projectDto.getLocation();

        // work-package, phase are from meta-data
        String workPackageRetrieved = "";
        String phaseRetrieved = "";

        // redcap-id from trigger
        final String redcapRecordId = "1";
        for (AttributeMapDTO attributeMapDto : retrievedById.getAttributes()) {
            switch (attributeMapDto.getKey()) {
                case ProjectDTO.WORK_PACKAGE_KEY :
                    workPackageRetrieved = attributeMapDto.getValue();
                    break;
                case ProjectDTO.PHASE_KEY :
                    phaseRetrieved = attributeMapDto.getValue();
                    break;
                default:
                    break;
            }
        }

        // assert retrieved data
        assertEquals(workPackage, workPackageRetrieved);
        assertEquals(phase, phaseRetrieved);
        assertEquals(projectLocation, locationRetrieved);

        // create a new Subject
        SubjectDTO newSubject = new SubjectDTO();
        newSubject.setLogin("53d8a54a"); // will be removed
        newSubject.setProject(retrievedById); // set retrieved project
        newSubject.setExternalId(redcapRecordId); // set redcap-record-id

        // create human-readable-id
        String humanReadableId = String.join("-", workPackageRetrieved, phaseRetrieved,
                locationRetrieved, redcapRecordId);

        //set meta-data to subject
        newSubject.setAttributes(Collections.singletonMap(SubjectDTO.HUMAN_READABLE_IDENTIFIER_KEY,
                humanReadableId));

        // create/save a subject
        // PUT api/subjects/
        SubjectDTO savedSubject = subjectService.createSubject(newSubject);
        assertThat(savedSubject.getId() > 0);

        // asset human-readable-id
        for (Map.Entry<String, String> attr : savedSubject.getAttributes().entrySet()) {
            if (SubjectDTO.HUMAN_READABLE_IDENTIFIER_KEY .equals(attr.getKey())) {
                assertEquals(humanReadableId, attr.getValue());
            }
        }
    }
}
