grammar Dsmr;

// Basic form of a telegram

//      /XXXZ Ident CR LF CR LF Data ! CRC CR LF

NEWLINE: '\r\n';
WORD: [a-z]+ ;


telegram: NEWLINE;
