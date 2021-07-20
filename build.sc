import mill._, scalalib._
import os.Path
import $file.firrtl.build
import $file.chisel3.build

/**
 * Scala 2.12 module that is source-compatible with 2.11.
 * This is due to Chisel's use of structural types. See
 * https://github.com/freechipsproject/chisel3/issues/606
 */
trait HasXsource211 extends ScalaModule {
  override def scalacOptions = T {
    super.scalacOptions() ++ Seq(
      "-deprecation",
      "-unchecked",
    )
  }
}

object firrtlXS extends firrtl.build.firrtlCrossModule("2.12.13") {
  override def millSourcePath = os.pwd / "firrtl"
}

object chiselSrc extends chisel3.build.chisel3CrossModule("2.12.13") {
  override def millSourcePath = os.pwd / "chisel3"
  def firrtlModule: Option[PublishModule] = Some(firrtlXS)
}

trait HasChisel3 extends ScalaModule {
  override def moduleDeps = super.moduleDeps ++ Seq(chiselSrc)
}

trait HasChiselTests extends CrossSbtModule  {
  object test extends Tests {
    override def ivyDeps = Agg(ivy"org.scalatest::scalatest:3.0.4", ivy"edu.berkeley.cs::chisel-iotesters:1.2+")
    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }
}

trait HasMacroParadise extends ScalaModule {
  // Enable macro paradise for @chiselName et al
  val macroPlugins = Agg(ivy"org.scalamacros:::paradise:2.1.1")
  def scalacPluginIvyDeps = macroPlugins
  def compileIvyDeps = macroPlugins
}

object chiselModule extends CrossSbtModule with HasChisel3 with HasChiselTests with HasXsource211 with HasMacroParadise {
  override def scalaVersion = "2.12.13"
  override def crossScalaVersion = "2.12.13"
}

