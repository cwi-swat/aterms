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
