#!/exlibris/product/bin/perl

#Definování knihovnen/include
use utf8;
use DBI qw(:sql_types);
use CGI qw(:standard);
use XML::Simple qw(:strict);
use Data::Dumper;
use POSIX qw(strftime);

{
#Definování proměnných
  my $dbh_aleph;
  my $sql;
  my $request = "";
  my $request_xml = "";
  my $response = "";

#Otevře log a přidává na konec souboru
  open(OUTFILE,">>/exlibris/aleph/a22_1/log/circ-logger-30.log");

#Čtení požadavku co příjde z aplikace
  read(STDIN,$request,$ENV{"CONTENT_LENGTH"});
#Když je požadavek prázdný vloží základní XML
  if ( $request eq "" ) {
    $request = "<circLoggerRequest30></circLoggerRequest30>";
  }

  $log_line = $request;
#Tvrdé konce řádek nahrazeny mezerami  
  $log_line =~ s/\n/ /g;
#Více mezer nahrazeno jednou  
  $log_line =~ s/  */ /g;
#Celý request zapsán do logu  
  print OUTFILE strftime("%Y-%m-%d %H:%M:%S ===request=== $log_line\n", localtime());

#z xml vygrabuje barcode
  $xml = new XML::Simple(ForceArray => 0, KeyAttr => {}, KeepRoot => 1,);
  $request_xml = $xml->XMLin($request);
  my $barcode = $request_xml->{'circLoggerRequest30'}->{'barcode'};

# pokud je barcode prázdný vytvoří standartní odpověď NACK 
  if ( $barcode eq "" ) {
    $response = "<?xml version='1.0' encoding='UTF-8'?>\n";
    $response .= "<response>\n";
    $response .= "<status>NACK</status>\n";
    $response .= "</response>\n";

  } else {
# Pokusí se připojit k oracle ajko aleph
    $ENV{"ORACLE_SID"}="aleph22";
    $ENV{"NLS_LANG"}="AMERICAN_AMERICA.UTF8";
    $dbh_aleph = DBI->connect( 'dbi:Oracle:', 'JMENO', 'HESLO', {RaiseError => 1, AutoCommit => 1})
      || die "Content-type: text/xml\n\n<?xml version='1.0' encoding='UTF-8'?>\n<response><chyba><kod>2001</kod><zprava>Oracle connection error: $DBI::errstr</zprava></chyba></response>";
# Když se mu to nepovede tak napíše chyba s textem a pošle a ukončí akci
# Naplní proměnou rec_key obsahem sloupce rec_key v ora tabulce z30 (jednotky)
    ($z309_rec_key_3) = $dbh_aleph -> selectrow_array("select Z30_REC_KEY from stk50.z30 where Z30_BARCODE = '".$barcode."'");

    if ( z309_rec_key_3 eq "" ) {

      $response = "<?xml version='1.0' encoding='UTF-8'?>\n";
      $response .= "<response>\n";
      $response .= "<status>NACK</status>\n";
      $response .= "</response>\n";

    } else {

      my $z309_rec_key = "STK         ".strftime( q/%Y%m%d%H%M%S/, localtime())."0";
# Přidá IP adresu toho kdo volá adresu
      my $z309_cataloger_ip = $ENV{"REMOTE_ADDR"};
# Datum a čas bez vteřin      
      my $z309_date_x = strftime( q/%Y%m%d%H%M/, localtime());
# SQL příkaz na vložení do z309 (Circulation Logger)
      $sql = "insert into stk50.z309(Z309_REC_KEY,Z309_REC_KEY_2,Z309_REC_KEY_3,Z309_REC_KEY_4,Z309_CATALOGER_NAME,Z309_CATALOGER_IP,Z309_DATE_X,Z309_ACTION,Z309_OVERRIDE,Z309_TEXT,Z309_DATA) values (";
      $sql .= "'$z309_rec_key','000000000',";
      $sql .= "'$z309_rec_key_3',";
      $sql .= "'            000000000000000',";
      $sql .= "'WEBSERVICE',";
      $sql .= "'$z309_cataloger_ip',";
      $sql .= "'$z309_date_x','30','N',";
      $sql .= "'Prezenční použití',";
      $sql .= "'Jednotka využita v části s volným výběrem')";
# celé sql dotaz si hodí do logfile
      print OUTFILE strftime("%Y-%m-%d %H:%M:%S ===sql=== $sql\n", localtime());
# provede SQL
      $dbh_aleph -> do ("$sql");

      $response = "<?xml version='1.0' encoding='UTF-8'?>\n";
      $response .= "<response>\n";
      $response .= "<status>ACK</status>\n";
      $response .= "</response>\n";

    }

  }
# hlavička http že jde o xml
  print "Content-type: text/xml\n\n";
# obsah xml 
  print "$response";
# ukončen log
  close(OUTFILE);

  exit (0);

}

