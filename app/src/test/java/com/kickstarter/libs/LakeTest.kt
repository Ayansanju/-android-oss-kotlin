package com.kickstarter.libs

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.User
import org.joda.time.DateTime
import org.json.JSONArray
import org.junit.Test
import rx.subjects.BehaviorSubject

class LakeTest : KSRobolectricTestCase() {

    private val propertiesTest = BehaviorSubject.create<Map<String, Any>>()

    @Test
    fun testDefaultProperties() {
        val client = MockTrackingClient(MockCurrentUser(), mockCurrentConfig(), true)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = Koala(client)

        lake.trackAppOpen()

        this.lakeTest.assertValue("App Open")

        assertDefaultProperties(null)
    }

    @Test
    fun testDefaultProperties_LoggedInUser() {
        val user = user()
        val client = MockTrackingClient(MockCurrentUser(user), mockCurrentConfig(), true)
        client.eventNames.subscribe(this.lakeTest)
        client.eventProperties.subscribe(this.propertiesTest)
        val lake = Koala(client)

        lake.trackAppOpen()

        this.lakeTest.assertValue("App Open")

        assertDefaultProperties(user)
    }

    private fun assertDefaultProperties(user: User?) {
        val expectedProperties = propertiesTest.value
        assertEquals("uuid", expectedProperties["session_android_uuid"])
        assertEquals(9999, expectedProperties["session_app_build_number"])
        assertEquals("9.9.9", expectedProperties["session_app_release_version"])
        assertEquals("native", expectedProperties["session_client_type"])
        assertEquals(JSONArray().put("android_example_experiment[control]"), expectedProperties["session_current_variants"])
        assertEquals("uuid", expectedProperties["session_device_distinct_id"])
        assertEquals("phone", expectedProperties["session_device_format"])
        assertEquals("Google", expectedProperties["session_device_manufacturer"])
        assertEquals("Pixel 3", expectedProperties["session_device_model"])
        assertEquals("Portrait", expectedProperties["session_device_orientation"])
        assertEquals("en", expectedProperties["session_display_language"])
        assertEquals(JSONArray().put("android_example_feature"), expectedProperties["session_enabled_features"])
        assertEquals(false, expectedProperties["session_is_voiceover_running"])
        assertEquals("kickstarter_android", expectedProperties["session_mp_lib"])
        assertEquals("Android 9", expectedProperties["session_os_version"])
        assertEquals(DateTime.parse("2018-11-02T18:42:05Z").millis / 1000, expectedProperties["session_time"])
        assertEquals("agent", expectedProperties["session_user_agent"])
        assertEquals(user != null, expectedProperties["session_user_logged_in"])
        assertEquals(false, expectedProperties["session_wifi_connection"])
    }

    private fun mockCurrentConfig() = MockCurrentConfig().apply {
        val config = ConfigFactory.configWithFeatureEnabled("android_example_feature")
                .toBuilder()
                .abExperiments(mapOf(Pair("android_example_experiment", "control")))
                .build()
        config(config)
    }

    private fun user() =
            UserFactory.user()
                    .toBuilder()
                    .id(15)
                    .backedProjectsCount(3)
                    .createdProjectsCount(2)
                    .starredProjectsCount(10)
                    .build()

}
