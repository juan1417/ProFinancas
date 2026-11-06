#!/usr/bin/env python
import os
import sys

# Asegurar que el backend este en el path
BACK_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "back")
sys.path.insert(0, BACK_DIR)
os.chdir(BACK_DIR)

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "profinancas.settings")

from django.core.management import execute_from_command_line

execute_from_command_line(sys.argv)
