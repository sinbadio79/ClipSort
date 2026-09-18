package com.clipsort.app.presentation

import android.app.Application
import com.clipsort.app.presentation.common.sharedHttpUrl
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class ClipLinksTest {
    @Test fun `extracts links from older saved captions`() {
        assertThat(sharedHttpUrl("Regarde ceci : https://youtu.be/one?t=3 !")).isEqualTo("https://youtu.be/one?t=3")
    }
    @Test fun `does not accept scripts or non web schemes`() {
        assertThat(sharedHttpUrl("javascript:alert(1)")).isNull()
        assertThat(sharedHttpUrl("intent://something")).isNull()
    }
    @Test fun `removes enclosing punctuation and normalizes scheme`() {
        assertThat(sharedHttpUrl("(HTTPS://youtu.be/one).")).isEqualTo("https://youtu.be/one")
    }
}
