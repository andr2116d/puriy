provider "aws" {
  region  = var.region
  profile = var.perfil_aws

  default_tags {
    tags = {
      Proyecto   = var.prefijo
      Ambiente   = var.ambiente
      Gestionado = "terraform"
    }
  }
}
