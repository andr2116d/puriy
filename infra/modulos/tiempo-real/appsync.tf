data "aws_region" "actual" {}

resource "aws_appsync_api" "eventos" {
  name = var.nombre

  event_config {
    auth_provider {
      auth_type = "AWS_IAM"
    }

    auth_provider {
      auth_type = "AMAZON_COGNITO_USER_POOLS"

      cognito_config {
        aws_region   = data.aws_region.actual.region
        user_pool_id = var.grupo_usuarios_id
      }
    }

    connection_auth_mode {
      auth_type = "AMAZON_COGNITO_USER_POOLS"
    }

    default_publish_auth_mode {
      auth_type = "AWS_IAM"
    }

    default_subscribe_auth_mode {
      auth_type = "AMAZON_COGNITO_USER_POOLS"
    }
  }
}

resource "aws_appsync_channel_namespace" "asientos" {
  api_id = aws_appsync_api.eventos.api_id
  name   = "asientos"
}
