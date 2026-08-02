#!/bin/bash
# Auto to script trexei MESA se ena voithitiko container ("db-init"), META pou to SQL Server
# einai idi etoimo (healthy). Doulia tou: na dimiourgisei ti vasi "maintrackdb" an den yparxei idi
# (o Docker SQL Server container ftiaxnei mono tou tis "systemikes" vaseis - master, tempdb ktl -
# oxi mia dikia mas onomasmeni vasi, auto prepei na to kanoume emeis).
set -e

# Diaforetikes ekdoseis tis eikonas (image) tou SQL Server exoun to sqlcmd se diaforetiko fakelo -
# dokimazoume proto to neotero (mssql-tools18) kai an den yparxei, pigainoume sto palio (mssql-tools).
SQLCMD=/opt/mssql-tools18/bin/sqlcmd
if [ ! -f "$SQLCMD" ]; then
  SQLCMD=/opt/mssql-tools/bin/sqlcmd
fi

echo "Xrisimopoioume sqlcmd apo: $SQLCMD"

"$SQLCMD" -S sqlserver -U sa -P "$SA_PASSWORD" -C \
  -Q "IF DB_ID('maintrackdb') IS NULL CREATE DATABASE maintrackdb;" \
  || "$SQLCMD" -S sqlserver -U sa -P "$SA_PASSWORD" \
  -Q "IF DB_ID('maintrackdb') IS NULL CREATE DATABASE maintrackdb;"

echo "I vasi 'maintrackdb' einai etoimi."
