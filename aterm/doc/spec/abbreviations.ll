; 
;     ATerm -- The ATerm (Annotated Term) library
;     Copyright (C) 1998-2000  Stichting Mathematisch Centrum, Amsterdam, 
;                              The  Netherlands.
; 
;     This program is free software; you can redistribute it and/or modify
;     it under the terms of the GNU General Public License as published by
;     the Free Software Foundation; either version 2 of the License, or
;     (at your option) any later version.
; 
;     This program is distributed in the hope that it will be useful,
;     but WITHOUT ANY WARRANTY; without even the implied warranty of
;     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;     GNU General Public License for more details.
; 
;     You should have received a copy of the GNU General Public License
;     along with this program; if not, write to the Free Software
;     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
; 
;; file:            abbreviations.ll
;;
;; custom abbreviations for tolatex
;;
;; specification:   
;; author:          
;; date:            
;;
;; ToLaTeX options
;;
(defvar #:TOLATEX:Notation-Files ()) ; generate .not and .imp files if true
(defvar #:TOLATEX:all-modules ())    ; generate code for all modules
;;
;;
;; The abbreviations file contains a list mapping literals
;; to \LaTeX\ symbols that can be adapted by the user.
;; The file is read on every run of To\LaTeX and a default
;; file is generated if none exists.
;;
;;
(defvar #:TOLATEX:custom-abbreviations
  (list
;;
;; The entries of the list are lists containing two 
;; strings. If the first one is recognized as a literal
;; it is replace by the second one.
;;
;; Example:
;;
   '("<=" "\le")
;;
;; The default abbreviations-list maps <= to a double
;; left arrow, instead of the less-equal sign.
;;
;; Comment is written by the character ;. If you want
;; to temporarily undo an entry, comment it.
;;
;; Add your own definitions below


;; End of abbreviations list
  )
)

;; Skeleton Abbreviations
(defvar #:TOLATEX:custom-skel-abbreviations
  (list
   (list '("(" son ")")  
         '(copy "\SLEX{(}" son "\SLEX{)}") )
   (list '("if" son "then" son "else" son) 
         '(copy        "{\fbold if}\HSPACE"   son 
                "\HSPACE{\fbold then}\HSPACE" son 
                "\HSPACE{\fbold else}\HSPACE" son) )
   (list '("if" son "then" son) 
         '(copy         "{\fbold if}\HSPACE"   son 
                 "\HSPACE{\fbold then}\HSPACE" son ) )
   (list '("(" son "#" son ")")  
         '(son "\times" son) )
  )
)
;; End of skeleton abbreviations
