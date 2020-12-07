import com.amazonaws.services.iot.client.AWSIotMqttClient
import sampleUtil.SampleUtil
import java.util.*

class AWS {
    companion object {
        private val random = Random()
    }

    fun connect(){
        val clientEndpoint = "a377sjyuqggau9-ats.iot.us-east-1.amazonaws.com";
        val clientId = random.nextInt(1000000000).toString()
        val certificateFile = "certificate.pem.crt";
        val privateKeyFile = "private.pem.key";

        val pair = SampleUtil.getKeyStorePasswordPair(certificateFile, privateKeyFile);
        val client = AWSIotMqttClient(clientEndpoint, clientId, pair.keyStore, pair.keyPassword)

        client.connect();
        println("connected")
        client.publish("/", "hello world")
        println("published")
    }


}