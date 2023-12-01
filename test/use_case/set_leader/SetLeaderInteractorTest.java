package use_case.set_leader;

import data_access.FirebaseAccessObject;
import entity.CommonProject;
import entity.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import use_case.set_leader.SetLeaderInputBoundary;
import use_case.set_leader.SetLeaderInputData;
import use_case.set_leader.SetLeaderInteractor;
import use_case.set_leader.SetLeaderOutputBoundary;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SetLeaderInteractorTest {

    @Mock
    private FirebaseAccessObject mockFirebaseAccessObject;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests the successful setting of a new leader for a project.
     * Verifies that the new leader's email is correctly updated in the project's details
     * by mocking the FirebaseAccessObject and asserting the interaction.
     */
    @Test
    void successTest() {
        // Arrange
        String projectName = "TestProject";
        String newLeaderEmail = "test@example.com";
        CommonProject project = new CommonProject(projectName, newLeaderEmail, new ArrayList<>());
        when(mockFirebaseAccessObject.getProjectInfo(projectName)).thenReturn(project);

        SetLeaderInputData inputData = new SetLeaderInputData(projectName, newLeaderEmail);

        SetLeaderOutputBoundary successPresenter = new SetLeaderOutputBoundary() {
            @Override
            public void prepareSuccessView(String new_leader) {
                assertEquals(project.getLeaderEmail(), newLeaderEmail);
            }

            @Override
            public void prepareFailView(String error) {
                fail("Setting a new leader should be successful.");
            }
        };

        SetLeaderInputBoundary interactor = new SetLeaderInteractor(mockFirebaseAccessObject, successPresenter);

        // Act
        interactor.updateProjectDetails(inputData);

        // Assert
        verify(mockFirebaseAccessObject).getProjectInfo(projectName);
    }

    /**
     * Tests if an error is raised when trying to set a new leader who is the same as the current leader.
     * Verifies that such an action does not succeed and appropriately triggers a failure response
     * using a mocked FirebaseAccessObject.
     */
    @Test
    void sameLeaderErrorTest() {
        // Arrange
        String projectName = "TestProject";
        String originalLeader = "leader@example.com";
        CommonProject project = new CommonProject(projectName, originalLeader, new ArrayList<>());
        when(mockFirebaseAccessObject.getProjectInfo(projectName)).thenReturn(project);

        SetLeaderInputData inputData = new SetLeaderInputData(projectName, originalLeader);

        SetLeaderOutputBoundary failurePresenter = new SetLeaderOutputBoundary() {
            @Override
            public void prepareSuccessView(String new_leader) {
                fail("Setting the same leader should not be successful.");
            }

            @Override
            public void prepareFailView(String error) {
                assertEquals("New leader is the same as the current leader", error);
            }
        };

        SetLeaderInputBoundary interactor = new SetLeaderInteractor(mockFirebaseAccessObject, failurePresenter);

        // Act
        interactor.updateProjectDetails(inputData);

        // Assert
        verify(mockFirebaseAccessObject).getProjectInfo(projectName);
    }
}
