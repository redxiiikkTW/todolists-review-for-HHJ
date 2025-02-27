package com.example.todolists_springboot.contrller;

import com.example.todolists_springboot.controller.TaskController;
import com.example.todolists_springboot.domain.Task;
import com.example.todolists_springboot.domain.User;
import com.example.todolists_springboot.handler.exception.TaskNotFoundException;
import com.example.todolists_springboot.service.TaskService;
import com.example.todolists_springboot.service.AssignmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureJsonTesters
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TaskService taskService;
    @MockBean
    private AssignmentService assignmentService;
    @Autowired
    private JacksonTester<Task> taskJson;
    @Autowired
    private JacksonTester<List<Task>> taskListJson;
    @Autowired
    private JacksonTester<User> userJson;
    @Autowired
    private JacksonTester<List<User>> userListJson;

    @Test
    void should_add_a_task_and_return_the_task() throws Exception {
        Task requestBody = new Task("test");
        MockHttpServletRequestBuilder requestBuilder = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson.write(requestBody).getJson());
        MockHttpServletResponse response = mockMvc.perform(requestBuilder)
                .andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        verify(taskService).addTask(requestBody);
    }

    @Test
    void should_get_all_the_tasks() throws Exception {
        Task test = new Task(1L, "test", false);
        when(taskService.getTasks(null)).thenReturn(List.of(test));
        MockHttpServletResponse response = mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(taskListJson.write(List.of(test)).getJson());
        verify(taskService).getTasks(null);
    }

    @Test
    void should_get_the_task_by_the_task_id_when_the_task_exists() throws Exception {
        Task test = new Task(1L, "test", false);
        when(taskService.getTaskByTaskId(1L)).thenReturn(test);
        MockHttpServletResponse response = mockMvc.perform(get("/api/tasks/{id}",1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(taskJson.write(test).getJson());
        verify(taskService).getTaskByTaskId(1L);
    }
    @Test
    void should_get_the_task_by_the_task_id_when_the_task_not_exist() {
        Task test = new Task(1L, "test", false);
        doThrow(new TaskNotFoundException()).when(taskService).getTaskByTaskId(1L);

        MockHttpServletResponse response = null;
        try {
            response = mockMvc.perform(get("/api/tasks/{id}", 1L))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn()
                    .getResponse();
        } catch (Exception e) {
            if (e instanceof TaskNotFoundException) {
                assertThat(e.getMessage()).isEqualTo("Task not found");
            }
        }

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        verify(taskService).getTaskByTaskId(1L);
    }
    @Test
    void should_update_the_task_by_id_when_the_task_exists() throws Exception {
        Task requestBody = new Task(null, "test test", true);
        Task newTask = new Task(1L, "test test", true);
        when(taskService.updateTask(1L, requestBody)).thenReturn(newTask);

        MockHttpServletRequestBuilder requestBuilder = put("/api/tasks/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson.write(requestBody).getJson());
        MockHttpServletResponse response = mockMvc.perform(requestBuilder)
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(taskJson.write(newTask).getJson());
        verify(taskService).updateTask(1L, requestBody);
    }

    @Test
    void should_update_the_task_by_id_when_the_task_not_exist() {
        Task requestBody = new Task("test test", true);
        when(taskService.updateTask(1L, requestBody)).thenThrow(new TaskNotFoundException());
        MockHttpServletResponse response = null;
        try {
            MockHttpServletRequestBuilder requestBuilder = put("/api/tasks/{id}", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(taskJson.write(requestBody).getJson());
            response = mockMvc.perform(requestBuilder)
                    .andReturn()
                    .getResponse();
        } catch (Exception e) {
            if (e instanceof TaskNotFoundException) {
                assertThat(e.getMessage()).isEqualTo("Task not found");
            }
        }

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        verify(taskService).updateTask(1L, requestBody);
    }

    @Test
    void should_delete_the_task_when_the_task_exits() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = delete("/api/tasks/{id}", 1L);
        MockHttpServletResponse response = mockMvc.perform(requestBuilder)
                .andReturn()
                .getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void should_delete_the_task_when_the_task_does_not_exit() {
        doThrow(new TaskNotFoundException()).when(taskService).deleteTask(1L);

        MockHttpServletResponse response = null;
        try {
            MockHttpServletRequestBuilder requestBuilder = delete("/api/tasks/{id}", 1L);
            response = mockMvc.perform(requestBuilder)
                    .andReturn()
                    .getResponse();
        } catch (Exception e) {
            if (e instanceof TaskNotFoundException) {
                assertThat(e.getMessage()).isEqualTo("Task not found");
            }
        }

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void should_select_tasks_by_keyword_and_return_the_task() throws Exception {
        Task task = new Task(1L, "test", true);
        when(taskService.getTasksByKeyword("te")).thenReturn(List.of(task));

        MockHttpServletResponse response = mockMvc.perform(get("/api/tasks/keyword-text/{keyword}", "te"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(taskListJson.write(List.of(task)).getJson());
        verify(taskService).getTasksByKeyword("te");
    }

    @Test
    void should_get_all_users_of_the_task_by_task_name() throws Exception {
        List<User> theReturnedUsers = List.of(new User(1L, "test"), new User(2L, "test2"));
        when(assignmentService.getUsersOfTaskByTaskName("test")).thenReturn(theReturnedUsers);

        MockHttpServletResponse response = mockMvc.perform(get("/api/tasks/{name}/users", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(userListJson.write(theReturnedUsers).getJson());
        verify(assignmentService).getUsersOfTaskByTaskName("test");
    }
}
