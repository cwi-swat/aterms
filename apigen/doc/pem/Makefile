

all: pem.ps

pem.dvi: pem.ltx overview.eps *.sdf *.api *.pseudo *.adt *.h
	latex pem.ltx

pem.ps: pem.dvi overview.eps
	dvips pem.dvi -o pem.ps
	

%.eps: %.fig
	fig2dev -Leps $< $@
