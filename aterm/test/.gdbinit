# $Id$

define termtype
  set $type = ($arg0->header & ((1<<5) | (1<<6) | (1<<7))) >> 5
  if $type == 0
    printf "AT_FREE\n"
  else
    if $type == 1
      printf "AT_INT\n"
    else
      if $type == 2
        printf "AT_REAL\n"
      else
        if $type == 3
          printf "AT_APPL\n"
        else
          if $type == 4
            printf "AT_LIST\n"
          else
            if $type == 5
              printf "AT_PLACEHOLDER\n"
            else
              if $type == 6
                printf "AT_BLOB\n"
              else
                printf "*illegal*\n"
 	      end
            end
          end
        end
      end
    end
  end
end

document termtype
  Print the type of a term
end

define termanno
  if $arg0->header & 1
    printf "yes"
  else
    printf "no"
  end
end

document termanno
  Check if a term is annotated
end

define termarity
  set $arity = ($arg0->header & ((1<<2) | (1<<3) | (1<<4))) >> 2
  print $arity
end

document termarity
  Print the (inlined) arity of a term.
end

define termsymbol
  set $type = ($arg0->header & ((1<<5) | (1<<6) | (1<<7))) >> 5
  print *lookup_table[$arg0->header >> 8]
  if $type != 3
    printf " (warning: not a AT_APPL)"
  end
end

document termsymbol
  Print the symbol of a term.
end

define termarg
	set $arg = ((ATerm *)$arg0)[2 + $arg1]
	call ATprintf("%t\n", $arg)
end

document termarg
	Print a specific argument of a term.
end

define tprint
	call ATprintf("%t\n", $arg0)
end

document tprint
	Print a specific term.
end
