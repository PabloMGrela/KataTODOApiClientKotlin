package com.karumi.todoapiclient

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import todoapiclient.TodoApiClient
import todoapiclient.dto.TaskDto
import todoapiclient.exception.ItemNotFoundError
import todoapiclient.exception.UnknownApiError

class TodoApiClientTest : MockWebServerTest() {

    /**
    GET ALL TASK
    -Request sent to the expected path X
    -Request sent with the expected headers X
    -Request sent with the expected method X
    -Response parsed properly
    +Empty list of task X
    +Non empty list X
    -Error
    +Server error 500 X
    +Timeout -> No internet connection

    GETS TASK BY ID
    -Request contains the id as part of the path /todos/{id} X
    -Parses the created task response properly X
    -Server error 500 X
    -Task not found 404 X
     */

    private lateinit var apiClient: TodoApiClient

    @Before
    override fun setUp() {
        super.setUp()
        val mockWebServerEndpoint = baseEndpoint
        apiClient = TodoApiClient(mockWebServerEndpoint)
    }

    @Test
    fun sendsAcceptAndContentTypeHeaders() {
        enqueueMockResponse(200, "getTasksResponse.json")

        apiClient.allTasks

        assertRequestContainsHeader("Accept", "application/json")
    }

    @Test
    fun sendsContentTypeHeader() {
        enqueueMockResponse(200, "getTasksResponse.json")

        apiClient.allTasks

        assertRequestContainsHeader("Content-Type", "application/json")
    }

    @Test
    fun parsesAnEmptyResponse() {
        enqueueMockResponse(200, "emptyResponse.json")

        val tasks = apiClient.allTasks.right

        assertEquals(0, tasks?.size)

    }

    @Test
    fun sendsGetAllTaskRequestToTheCorrectEndpoint() {
        enqueueMockResponse(200, "getTasksResponse.json")

        apiClient.allTasks

        assertGetRequestSentTo("/todos")
    }

    @Test
    fun parsesTasksProperlyGettingAllTheTasks() {
        enqueueMockResponse(200, "getTasksResponse.json")

        val tasks = apiClient.allTasks.right!!

        assertEquals(200, tasks.size.toLong())
        assertTaskContainsExpectedValues(tasks[0])
    }

    @Test
    fun whenRequestIsMadeAndServerReturnsAnError() {
        enqueueMockResponse(500, null)

        val error = apiClient.allTasks.left

        assertEquals(UnknownApiError(500), error)
    }

    @Test
    fun requestToGetOneTaskHasCorrectPath() {
        enqueueMockResponse(200, "getTaskByIdResponse.json")

        apiClient.getTaskById("1")

        assertGetRequestSentTo("/todos/1")
    }

    @Test
    fun parsesTaskProperlyGettingOneTask() {
        enqueueMockResponse(200, "getTaskByIdResponse.json")

        val task = apiClient.getTaskById("1")

        assertTaskContainsExpectedValues(task.right)
    }

    @Test
    fun whenRequestIsMadeToGetOneTaskAndServerReturnsAnError() {
        enqueueMockResponse(500, null)

        val error = apiClient.getTaskById("1").left

        assertEquals(UnknownApiError(500), error)
    }

    @Test
    fun whenRequestIsMadeToGetOneTaskAndTaskIsNotFoundAnErrorIsReceived() {
        enqueueMockResponse(404, null)

        val error = apiClient.getTaskById("0").left

        assertEquals(ItemNotFoundError, error)
    }


    private fun assertTaskContainsExpectedValues(task: TaskDto?) {
        assertTrue(task != null)
        assertEquals(task?.id, "1")
        assertEquals(task?.userId, "1")
        assertEquals(task?.title, "delectus aut autem")
        assertFalse(task!!.isFinished)
    }
}
