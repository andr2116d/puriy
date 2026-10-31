resource "aws_cognito_user_pool" "usuarios" {
  name                     = var.nombre
  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]
  mfa_configuration        = "OPTIONAL"

  software_token_mfa_configuration {
    enabled = true
  }

  password_policy {
    minimum_length    = 12
    require_lowercase = true
    require_uppercase = true
    require_numbers   = true
    require_symbols   = false
  }
}

resource "aws_cognito_user_pool_client" "web" {
  name                          = "${var.nombre}-web"
  user_pool_id                  = aws_cognito_user_pool.usuarios.id
  generate_secret               = false
  prevent_user_existence_errors = "ENABLED"
  explicit_auth_flows           = ["ALLOW_USER_SRP_AUTH", "ALLOW_REFRESH_TOKEN_AUTH"]
}

resource "aws_cognito_user_group" "grupos" {
  for_each = toset(["clientes", "personal"])

  name         = each.value
  user_pool_id = aws_cognito_user_pool.usuarios.id
}
