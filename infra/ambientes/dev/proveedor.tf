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

provider "aws" {
  alias  = "global"
  region = "us-east-1"

  default_tags {
    tags = {
      Proyecto   = var.prefijo
      Ambiente   = var.ambiente
      Gestionado = "terraform"
    }
  }
}
