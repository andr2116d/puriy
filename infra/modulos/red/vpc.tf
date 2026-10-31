resource "aws_vpc" "principal" {
  cidr_block           = var.cidr
  enable_dns_hostnames = true

  tags = {
    Name = var.nombre
  }
}

resource "aws_internet_gateway" "principal" {
  vpc_id = aws_vpc.principal.id

  tags = {
    Name = var.nombre
  }
}

resource "aws_subnet" "publica" {
  for_each = var.subredes

  vpc_id                  = aws_vpc.principal.id
  availability_zone       = each.key
  cidr_block              = each.value.publica
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.nombre}-publica-${each.key}"
  }
}

resource "aws_subnet" "aplicacion" {
  for_each = var.subredes

  vpc_id            = aws_vpc.principal.id
  availability_zone = each.key
  cidr_block        = each.value.aplicacion

  tags = {
    Name = "${var.nombre}-aplicacion-${each.key}"
  }
}

resource "aws_subnet" "datos" {
  for_each = var.subredes

  vpc_id            = aws_vpc.principal.id
  availability_zone = each.key
  cidr_block        = each.value.datos

  tags = {
    Name = "${var.nombre}-datos-${each.key}"
  }
}
