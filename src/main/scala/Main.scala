import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.{Behaviors, Routers}
import api.{NodeRouter, Server}
import com.typesafe.config.{Config, ConfigFactory}
import node.Node
import org.slf4j.{Logger, LoggerFactory}
import model._
object Main {
  val config: Config = ConfigFactory.load()
  val address = config.getString("http.ip")
  val port = config.getInt("http.port")
  val nodeId = config.getString("clustering.ip")

  def main(args: Array[String]): Unit = {

    implicit val log: Logger = LoggerFactory.getLogger(getClass)

    val rootBehavior = Behaviors.setup[Node.Command] { context =>
      context.spawnAnonymous(Node())
      val group = Routers.group(Node.NodeServiceKey)
      val node = context.spawnAnonymous(group)
      val router = new NodeRouter(node)(context.system, context.executionContext)
      Server.startHttpServer(router.route, address, port)(context.system, context.executionContext)
      Behaviors.empty
    }
    ActorSystem[Node.Command](rootBehavior, "cluster-playground")

  }



}
