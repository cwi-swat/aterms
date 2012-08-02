# AX_C_FLOAT_WORD_ORDER_BIG ([ACTION-IF-TRUE], [ACTION-IF-FALSE], [ACTION-IF-UNKNOWN])
# -------------------------------------------------------------------------
AC_DEFUN(
	[AX_C_FLOAT_WORD_ORDER_BIG],
	[
		AC_CACHE_CHECK(
			whether float word ordering is big endian,
			ax_cv_c_float_word_order_big,
			[
				AC_RUN_IFELSE(
					[
						/* This code returns 0 if the float word order is big endian and >= 1 if it is little endian. */
						main(){
							#ifdef WORDS_BIGENDIAN
								return 0; /* If the system's encoding is big endian, so is the float word order. NOTE: If the encoding is big endian and WORDS_BIGENDIAN isn't defined, the code below will still return the correct float word order (big). */
							#else
								union
								{
									double d;
									/* IEEE754 little endian encoded floating point number structure with little endian float word order. */
									struct{
										unsigned int mantissa1:32;
										unsigned int mantissa0:20;
										unsigned int exponent:11;
										unsigned int negative:1;
									} ieee;
								} u;
								u.d = -1;
								return (u.ieee.negative == 1);
							#endif
						}
					],
					ax_cv_c_float_word_order_big=yes,
					ax_cv_c_float_word_order_big=no,
					$1
				)
			]
		)
		
		case $ax_cv_c_float_word_order_big in
			yes)
			    m4_default(
			    	[$1],
					[
						AC_DEFINE(
							[FLOAT_WORD_ORDER_BIG],
							1,
							[Define to 1 if your system stores words within floats with the most significant word first]
						)
					]
				) ;;
			no)
		    	$2 ;;
		  	*)
		    m4_default(
		    	[$3],
				[
					AC_MSG_ERROR(
						[Unable to determain float word ordering. You need to manually preset ax_cv_c_float_word_order_big=(yes / no).]
		    		)
		    	]
		    ) ;;
		esac
	]
)
# AX_C_FLOAT_WORD_ORDER_BIG