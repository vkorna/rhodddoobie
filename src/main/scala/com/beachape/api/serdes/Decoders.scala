package com.beachape.api.serdes

import cats.effect.Effect
import com.beachape.api.models.responses.InvalidMessageBodyFailure
import com.beachape.api.enums.ResourceKind
import com.beachape.ids.{DeploymentId, Ref, ResourceId}
import io.circe.Decoder
import org.http4s.{DecodeResult, EntityDecoder}
import org.http4s.circe._

/**
  * Holds a few Circe-backed JSON `Decoder` instances, as well as a deriver for http4s' `EntityDecoder` (bridge for
  * using the Circe instances for decoding to http4s endpoints) so that things that can be decoded from JSON by Circe
  * can be decoded in our routes.
  *
  * Note that in this project, we use full auto Circe derivation for the most part to reduce boilerplate.
  */
object Decoders extends JsonDecoders with Decoders

trait Decoders {

  implicit def entityDecoderFromJsonDecoder[F[_]: Effect, A: Decoder]: EntityDecoder[F, A] =
    jsonOf[F, A]
      .handleErrorWith(f => DecodeResult.failure(InvalidMessageBodyFailure(f.getMessage())))

}

trait JsonDecoders {

  implicit val deploymentIdJsonDecoder: Decoder[DeploymentId] =
    Decoder.decodeLong.map(DeploymentId.apply)

  implicit val resourceIdJsonDecoder: Decoder[ResourceId] =
    Decoder.decodeLong.map(ResourceId.apply)

  implicit val refIdJsonDecoder: Decoder[Ref] =
    Decoder.decodeString.map(Ref.apply)

  implicit val resourceKindDecoder: Decoder[ResourceKind] =
    Decoder.decodeString.emap { s =>
      ResourceKind.withNameOption(s) match {
        case Some(r) => Right(r)
        case None    => Left(s"Invalid value for ResourceKind: [$s]")
      }
    }

}
