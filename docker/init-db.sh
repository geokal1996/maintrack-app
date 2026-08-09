#!/bin/bash
# Auto to script trexei MESA se ena voithitiko container ("db-init"), META pou to SQL Server
# einai idi etoimo (healthy). Kanei dyo pragmata:
#   1) Dimiourgei ti vasi "maintrackdb" an den yparxei
#      (o Docker SQL Server ftiaxnei mono tis systemikes vaseis - master, tempdb ktl)
#   2) Dimiourgei ton xristi pou xrisimopoiei i efarmogi
set -e

# Diaforetikes ekdoseis tis eikonas (image) tou SQL Server exoun to sqlcmd se diaforetiko fakelo -
# dokimazoume proto to neotero (mssql-tools18) kai an den yparxei, pigainoume sto palio (mssql-tools).
SQLCMD=/opt/mssql-tools18/bin/sqlcmd
EXTRA="-C"
if [ ! -f "$SQLCMD" ]; then
  SQLCMD=/opt/mssql-tools/bin/sqlcmd
  EXTRA=""
fi

echo "Xrisimopoioume sqlcmd apo: $SQLCMD"

run_sql() {
  "$SQLCMD" -S sqlserver -U sa -P "$SA_PASSWORD" $EXTRA -b -Q "$1"
}

run_sql "IF DB_ID('maintrackdb') IS NULL CREATE DATABASE maintrackdb;"
echo "I vasi 'maintrackdb' einai etoimi."

# O xristis tis efarmogis. DEN xrisimopoioume ton "sa" gia tin efarmogi:
# o sa mporei na kanei OTIDIPOTE se olo ton server (na svisei alles vaseis, na
# ftiaxei xristes). I efarmogi xreiazetai dikaiomata MONO sti diki tis vasi.
# An pote diarrefsei o kodikos tis efarmogis, i zimia periorizetai se auti ti vasi.
if [ -n "$APP_DB_USERNAME" ] && [ -n "$APP_DB_PASSWORD" ]; then
  run_sql "
    IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = '$APP_DB_USERNAME')
        CREATE LOGIN [$APP_DB_USERNAME] WITH PASSWORD = '$APP_DB_PASSWORD', CHECK_POLICY = OFF;
  "
  run_sql "
    USE maintrackdb;
    IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = '$APP_DB_USERNAME')
        CREATE USER [$APP_DB_USERNAME] FOR LOGIN [$APP_DB_USERNAME];
    ALTER ROLE db_owner ADD MEMBER [$APP_DB_USERNAME];
  "
  echo "O xristis efarmogis '$APP_DB_USERNAME' einai etoimos."
fi

echo "Olokliromeno."
