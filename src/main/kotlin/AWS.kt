import com.amazonaws.services.iot.client.AWSIotMqttClient
import sampleUtil.SampleUtil
import java.util.*

class AWS {
    fun publish(topic: String, msg: String) {
        client.publish(topic, msg)
    }

    companion object {
        private val random = Random()
        private val client = connect()

        private fun connect() : AWSIotMqttClient {
            val clientEndpoint = "a377sjyuqggau9-ats.iot.us-east-1.amazonaws.com"
            val clientId = random.nextInt(1000000000).toString()
            val certificateFile = "certificate.pem.crt"
            val privateKeyFile = "private.pem.key"

            val pair = SampleUtil.getKeyStorePasswordPair(certificateFile, privateKeyFile)
            val client = AWSIotMqttClient(clientEndpoint, clientId, pair.keyStore, pair.keyPassword)

            client.connect()
            println("connected")

            return client
        }
    }
}