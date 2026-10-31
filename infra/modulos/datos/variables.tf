variable "nombre" {
  description = "Nombre base de los recursos"
  type        = string
}

variable "vpc_id" {
  description = "VPC"
  type        = string
}

variable "subredes" {
  description = "Subredes de datos"
  type        = list(string)
}

variable "clase_instancia" {
  description = "Clase de RDS"
  type        = string
  default     = "db.t4g.micro"
}

variable "almacenamiento_gb" {
  description = "Almacenamiento inicial"
  type        = number
  default     = 20
}
