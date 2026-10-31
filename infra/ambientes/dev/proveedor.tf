provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Proyecto   = var.prefijo
      Ambiente   = var.ambiente
      Gestionado = "terraform"
    }
  }
}
