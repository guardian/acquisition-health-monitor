import com.gu.{AppIdentity, AwsIdentity}
import com.gu.conf.{ConfigurationLoader}
import com.typesafe.config.Config

object AcquisitionAppConfiguration {

  private lazy val identity = AppIdentity.whoAmI("acquisition-health-monitor-api")
//  val config: Config = ConfigurationLoader.load(identity) {
//    case identity: AwsIdentity => S3ConfigurationLocation.default(identity)
//  }
}
