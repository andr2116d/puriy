terraform {
  required_version = ">= 1.10"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }

  backend "s3" {
    bucket       = "puriy-project-terraform-estado"
    key          = "global/terraform.tfstate"
    region       = "us-east-2"
    profile      = "ohio"
    encrypt      = true
    use_lockfile = true
  }
}
