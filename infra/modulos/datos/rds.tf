resource "aws_security_group" "base" {
  name        = "${var.nombre}-base"
  description = "PostgreSQL"
  vpc_id      = var.vpc_id
}

resource "aws_db_subnet_group" "base" {
  name       = var.nombre
  subnet_ids = var.subredes
}

resource "aws_db_instance" "base" {
  identifier                  = var.nombre
  engine                      = "postgres"
  engine_version              = "17"
  instance_class              = var.clase_instancia
  allocated_storage           = var.almacenamiento_gb
  storage_type                = "gp3"
  storage_encrypted           = true
  db_name                     = "puriy"
  username                    = "puriy_admin"
  manage_master_user_password = true
  multi_az                    = true
  db_subnet_group_name        = aws_db_subnet_group.base.name
  vpc_security_group_ids      = [aws_security_group.base.id]
  publicly_accessible         = false
  backup_retention_period     = 35
  copy_tags_to_snapshot       = true
  skip_final_snapshot         = true
  apply_immediately           = true
}
