%---------------------------------------------------------------------------
%Please  be  aware that the use of  this LaTeX class file is governed by the
%following conditions:
%
%Copyright
%
%Copyright (c) 1999 John Wiley & Sons, Ltd., Baffins Lane, Chichester, West
%Sussex, PO19 1UD, UK.   All rights reserved.
%
%Rules of Use
%
%This class file is made available for use by authors who wish to prepare an
%article for publication in the journal Software---Practice and Experience
%published by John Wiley & Sons Ltd.  The user may not exploit any part of
%the class file commercially.
%
%This class file is provided on an `as is'  basis, without warranties of any
%kind, either express or implied, including but not limited to warranties of
%title,   or  implied  warranties  of  merchantablility  or  fitness  for  a
%particular purpose.  There will be no duty on the author[s] of the software
%or  John Wiley & Sons Ltd to correct any errors or defects in the software.
%Any  statutory  rights you may have remain unaffected by your acceptance of
%these rules of use.
%---------------------------------------------------------------------------
%
% Created by Alistair Smith, Sunrise Setting Ltd, 13 April 1999
% Modified by John Palmer, John Wiley and Sons Ltd, 25 May 1999
% speauth.cls --- For Softw. Pract. exper.

\def\update{1999/06/11 v1.1a} 

\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{speauth}[\update\ Softw. Pract. Exper.]

\newcommand\hmmax{0}

\DeclareOption*{\PassOptionsToClass{\CurrentOption}{article}}
\ProcessOptions
\LoadClass{article}
%\RequirePackage{times}
%\RequirePackage[cmbold]{mathtime}
\RequirePackage{pifont,latexsym,ifthen,theorem}
\RequirePackage{amsfonts,amssymb,amsbsy,amsmath}
\RequirePackage[dvips]{graphics}
%\RequirePackage{bm}
\RequirePackage[errorshow]{tracefnt}

\renewcommand{\normalsize}{\fontsize{11}{12pt}\selectfont}
\renewcommand{\small}{\fontsize{9}{10pt}\selectfont}
\renewcommand{\footnotesize}{\fontsize{8}{9pt}\selectfont}
\renewcommand{\scriptsize}{\fontsize{8}{9.5pt}\selectfont}
\renewcommand{\tiny}{\fontsize{6}{7pt}\selectfont}
\renewcommand{\large}{\fontsize{12}{14pt}\selectfont}
\renewcommand{\Large}{\fontsize{14}{18pt}\selectfont}
\renewcommand{\LARGE}{\fontsize{17}{22pt}\selectfont}
\renewcommand{\huge}{\fontsize{20}{25pt}\selectfont}
\renewcommand{\Huge}{\fontsize{25}{30pt}\selectfont}

\newcommand{\titlesize}{\fontsize{18}{21pt}\selectfont}


%Page and general

\@twosidetrue
\frenchspacing
\flushbottom
\hoffset -39pt
\parindent 1em
\textwidth 34pc
%For title page
\textheight 540pt%48 lines on later pages
\topmargin 0pt
\headsep 12pt
\headheight 32pt
\footskip 22pt
\hyphenpenalty=1000
\columnsep 2pc
\pretolerance=8000
\tolerance=9500
\hbadness=8000
\vbadness=9000
\setlength{\abovedisplayskip}{8\p@ plus2\p@ minus5\p@}
\setlength{\belowdisplayskip}{\abovedisplayskip}
\setlength{\abovedisplayshortskip}{\z@ plus3\p@}
\setlength{\belowdisplayshortskip}{6\p@ plus3\p@ minus3\p@}
\setcounter{topnumber}{5}
\def\topfraction{1}
\setcounter{bottomnumber}{5}
\def\bottomfraction{.8}
\setcounter{totalnumber}{10}
\def\textfraction{0}
\def\floatpagefraction{.8}

\theorembodyfont{\itshape}
\theoremheaderfont{\normalfont\bfseries}
\setlength{\theorempreskipamount}{6pt plus 2pt}
\setlength{\theorempostskipamount}{6pt plus 2pt}

\begingroup \makeatletter
\gdef\th@plain{\normalfont\itshape
  \def\@begintheorem##1##2{%
        \item[\hskip\labelsep \theorem@headerfont ##1\ {##2.}]}%
\def\@opargbegintheorem##1##2##3{%
   \item[\hskip\labelsep \theorem@headerfont ##1\ ##2\ (##3)]}}
\endgroup

\def\refname{references}
\def\figurename{\textit{Figure}}
\def\tablename{{Table}}
\def\abstractname{SUMMARY}

% Title page

% print integer as a zero filled 2 digit field
% but if S[1-9], print it anyway [v1.1]
%\def\twodigits#1{\ifnum #1 < 10{0#1}\else{#1}\fi} % the previous version
\def\twodigits#1{\def\issuenumber{#1}%
                 \def\si{S1}\def\sii{S2}\def\siii{S3}%    )
                 \def\siv{S4}\def\sv{S5}\def\svi{S6}%     )constants 
                 \def\svii{S7}\def\sviii{S8}\def\six{S9}% )
                 \ifx\issuenumber\si{#1}\else%
                 \ifx\issuenumber\sii{#1}\else%    
                 \ifx\issuenumber\siii{#1}\else%
                 \ifx\issuenumber\siv{#1}\else%
                 \ifx\issuenumber\sv{#1}\else%   ---test nine special cases
                 \ifx\issuenumber\svi{#1}\else%
                 \ifx\issuenumber\svii{#1}\else%
                 \ifx\issuenumber\sviii{#1}\else%
                 \ifx\issuenumber\six{#1}\else%
                 \ifnum #1 < 10{0#1}\else{#1}\fi% the ordinary numeric case
                 \fi\fi\fi\fi\fi\fi\fi\fi\fi}


% print integer as a zero filled 4 digit field
\def\fourdigits#1{\ifnum #1 < 10{000#1}\else
  \ifnum #1 < 100{00#1}\else
    \ifnum #1 < 1000{0#1}\else{#1}\fi
  \fi
\fi}

% add an assumed century to a twodigit year [v1.1]
\def\century#1{\ifnum #1 <90{20#1}\else{19#1}\fi}

%SPE{first page}{last page}{vol}{issue}{year}
\def\SPE#1#2#3#4#5{
\gdef\@mymainhead{\parbox[b]{\textwidth}{{\footnotesize SOFTWARE---PRACTICE AND EXPERIENCE\hfill}\\
{\footnotesize\it Softw. Pract. Exper.,\ }\footnotesize {\bf#3}(#4), \ifnum #1 = #2 #2 \else #1--#2\fi\ (\century{#5})\hfill%
\scriptsize\textit{Prepared using \textsf{speauth.cls} [Version: \update]}}}
\setcounter{page}{#1}
\newcounter{pageone}
\setcounter{pageone}{#1}
\newcounter{lastpage}
\setcounter{lastpage}{#2}
\newcounter{pages}
\setcounter{pages}{1}
\addtocounter{pages}{\thelastpage}
\addtocounter{pages}{-\thepageone}
\gdef\mymainfoot{CCC 0038--0644/#5/%
\twodigits{#4}\fourdigits{#1}--\twodigits{\thepages}\$17.50}
\gdef\@mycopyright{Copyright \copyright\ \century{#5} John Wiley \& Sons, Ltd.}
\def\thevolume{#3}
\def\theissue{#4}
\def\theyear{#5}
}

\gdef\received#1{\gdef\@myreceive{Received #1}}
\gdef\revised#1{\gdef\@myrevise{Revised #1}}
\gdef\accepted#1{\gdef\@myaccepted{Accepted #1}}
\gdef\noreceived#1{\gdef\@myreceive{#1}}
\gdef\norevised#1{\gdef\@myrevise{#1}}
\gdef\noaccepted#1{\gdef\@myaccepted{#1}}

\def\ps@myplain{\let\@mkboth\@gobbletwo
\def\@oddhead{\@mymainhead}
\def\@evenhead{}
\def\@oddfoot{\parbox[b]{\textwidth}{\vspace*{18pt}%
\normalsize\mymainfoot\hfill\normalsize\textit{\@myreceive}\\
\normalsize\@mycopyright\hfill\normalsize\textit{\@myrevise}\\
\phantom{1}\hfill\textit{\@myaccepted}
}}
\def\@evenfoot{}
}

\thispagestyle{myplain}

\def\runningheads#1#2{\markboth{\lowercase{#1}}{\lowercase{#2}}}

\def\ps@spe{
\let\@mkboth\@gobbletwo
\def\@evenhead{\normalsize \thepage\hfill\scshape{\leftmark}\hfill\phantom{\thepage}}
\def\@oddhead{\normalsize \phantom{\thepage}\hfill\scshape{\rightmark}\hfill\thepage}
\def\@evenfoot{\parbox[t]{\textwidth}{{\footnotesize \@mycopyright}%
\hfill{\footnotesize\it Softw.\ Pract.\ %
Exper.,\ }\footnotesize{\bf\thevolume}(\theissue), \thepageone--\thelastpage\ %
(\century\theyear)\\ \scriptsize\textit{Prepared using \textsf{speauth.cls}}}}
\def\@oddfoot{\@evenfoot}
}

\pagestyle{spe}


%%%%%%%%%
%Title, author, etc

\def\title#1{\setlength{\textheight}{576pt}
\vspace*{1pt}\begin{center}%
\titlesize{#1}\par
\vspace*{10pt}}

\def\author#1{\vspace*{18pt}\normalsize{#1}\par}

\def\address#1{\small\textit{#1}\end{center}}

\def\summary{\vspace*{8pt}\hspace{-6pt}\hfill\small\textbf{SUMMARY}\hfill\mbox{}\\[3pt]%
\noindent\ignorespaces\small\begin{bfseries}}%

\def\endsummary{ \@mycopyright\end{bfseries}\endlist}

\def\abstract{\summary}
\def\endabstract{\endsummary}

\def\nosections{\vspace{30\p@ plus12\p@ minus12\p@}
\noindent\ignorespaces}

\def\keywords#1{\vspace{6pt}\noindent{\footnotesize\textsc{key words:}%
\quad #1}\vskip 0.1pt \normalsize\par\noindent}

%define section heads etc for style


\def\thesection{\arabic{section}}
\def\thesubsection{\thesection.\arabic{subsection}}
\def\thesubsubsection{\thesubsection.\arabic{subsubsection}}
%\def\theparagraph{\thesubsubsection.\arabic{paragraph}}
%\def\thesubparagraph{\theparagraph.\arabic{subparagraph}}

\renewcommand{\section}{\def\@afterheading{\global\@nobreaktrue
      \everypar{\if@nobreak
                   \global\@nobreakfalse
                   \clubpenalty \@M
                   \if@afterindent \else {\setbox\z@\lastbox}\fi
                 \else \clubpenalty \@clubpenalty
                    \everypar{}\fi}}
\makeatletter
\renewcommand{\@seccntformat}[1]{{\csname the##1\endcsname.}\hspace{1em}}
\makeatother
\@startsection
{section}%           the name
{1}%                 the level
{\z@}%               the indent
{-18pt plus -4pt minus -2pt}%  the beforeskip
{+6pt}%     the afterskip
{\centering\normalsize}}% the style 
%%
%%
\def\subsection{\def\@afterheading{}
\@startsection
{subsection}%           the name
{2}%                 the level
{\z@}%               the indent
{-18pt plus -4pt minus -2pt}%  the beforeskip
{+6pt}%     the afterskip
{\normalsize\bfseries}}
% the style 
%%
%%
\def\subsubsection{\def\@afterheading{}
\@startsection
{subsubsection}%           the name
{3}%                 the level
{\z@}%               the indent
{-18pt plus -4pt minus -2pt}%  the beforeskip
{+6pt}%     the afterskip
{\normalsize\itshape}}% the style 

%%%%%%%%%%%%%%%%
\def\enumerate{\ifnum \@enumdepth >3 \@toodeep\else
      \advance\@enumdepth \@ne
      \edef\@enumctr{enum\romannumeral\the\@enumdepth}\list
      {\csname label\@enumctr\endcsname}{\usecounter
        {\@enumctr}\itemsep 0pt\parsep 0pt
         \def\makelabel##1{\hss\llap{##1}}}\fi}
 
\let\endenumerate =\endlist

\def\itemize{\ifnum \@itemdepth >3 \@toodeep\else \advance\@itemdepth \@ne
\edef\@itemitem{labelitem\romannumeral\the\@itemdepth}%
\list{\csname\@itemitem\endcsname}{\itemsep 0pt\parsep 0pt
\def\makelabel##1{\hss\llap{##1}}}\fi}
 
\let\enditemize =\endlist
%%%%%%%%%%
\renewcommand{\footnoterule}{\kern 3\p@\hrule width 4pc\kern 2.6\p@}

\renewcommand{\thefootnote}{\fnsymbol{footnote}}
\long\def\@makefntext#1{\parindent 1em%
\noindent{$\m@th^{\@thefnmark}$}#1}

\def\corraddr#1{\footnotetext[1]{Correspondence to: #1}\stepcounter{footnote}}
\def\corrauth{\footnotemark[1]}

\def\cgsn#1#2{\footnotetext[0]{Contract/grant sponsor: #1; Contract/grant
number: #2.}}
\def\cgs#1{\footnotetext[0]{Contract/grant sponsor: #1.}}

\def\affilnum#1{${}^{#1}$}
\def\affil#1{${}^{#1}$}

\long\def\@makecaption#1#2{\vspace*{5pt}{%
\setbox0=\vbox{\small\itshape #1.\ #2}%
\setbox0=\vbox{\unvbox0 \setbox1=\lastbox%
\setbox1=\hbox to \textwidth{\hfill\unhbox1\hfill}\box1}%
\box0}\par}

\long\def\@maketabcaption#1#2{\vspace{5pt}{%
\setbox0=\vbox{\small\rm #1.\ #2}%
\setbox0=\vbox{\unvbox0 \setbox1=\lastbox%
\setbox1=\hbox to \textwidth{\hfill\unhbox1\hfill}\box1}%
\box0}}

\renewcommand{\table}{\let\@makecaption\@maketabcaption\@float{table}}

\renewcommand{\thetable}{\Roman{table}}

\newcommand{\ms}{\noalign{\vspace{3\p@ plus2\p@ minus1\p@}}}
\newcommand{\boldarrayrulewidth}{1\p@}

\def\topline{\noalign{\ifnum0=`}\fi\hrule \@height
\boldarrayrulewidth \futurelet \@tempa\@xhline}
\def\@xhline{\ifx\@tempa\hline\vskip \doublerulesep\fi
\ifnum0=`{\fi}}

% Weighted rules for tables
\newcommand{\toprule}{\ms\topline\ms}
\newcommand{\midrule}{\ms\hline\ms}
\newcommand{\bottomrule}{\ms\topline}

%%%%%%%%%%%%%%%%%%

%Acks
\def\ack{\vspace{12pt plus3pt minus3pt}
\begin{center}
{\normalsize\textsc{acknowledgement}}\end{center}\par\noindent}%

\def\acks{\vspace{12pt plus3pt minus3pt}
\begin{center}
{\normalsize\textsc{acknowledgements}}\end{center}\par\noindent}

\def\appendix#1#2{%
\setcounter{figure}{0}
\setcounter{table}{0}
\setcounter{equation}{0}
\def\thefigure{#1.\arabic{figure}}
\def\thetable{#1.\Roman{table}}
\def\theequation{#1.\arabic{equation}}
\section*{APPENDIX\ #1.\ #2}%
}

%references

\def\thebibliography#1{\vspace{12pt plus3pt minus3pt}
\begin{center}
{\normalsize\scshape references}\end{center}%
\list{{\arabic{enumi}}}{\def\makelabel##1{\hss{##1.}}\topsep=0\p@\parsep=0\p@
\partopsep=0\p@\itemsep=0\p@
\labelsep=1ex\itemindent=0\p@
\settowidth\labelwidth{\small[#1]}%
\leftmargin\labelwidth
\advance\leftmargin\labelsep
\advance\leftmargin -\itemindent
\usecounter{enumi}}\small
\def\newblock{\ }
\sloppy\clubpenalty4000\widowpenalty4000
\sfcode`\.=1000\relax}
\let\endthebibliography=\endlist

%Book reviews

\def\BR{\twocolumn[\vspace{12pt}\title{Book Review}\end{center}]}

\def\btitle#1{\noindent\hspace{12pt}\small%
\parbox{15pc}{#1}\vspace{1pc}\noindent}

\def\aug#1{\bigbreak
\begin{flushright}
\textit{#1}
\end{flushright}}

%Other macros
\def\dssty{\displaystyle}
\def\ssty{\scriptstyle}
\def\dfrac{\displaystyle\frac}
\def\bprop{\begin{prop}}
\def\eprop{\end{prop}}
\def\blem{\begin{lema}}
\def\elem{\end{lema}}
\def\bth{\begin{theo}}
\def\eth{\end{theo}}
\def\bcor{\begin{cor}}
\def\ecor{\end{cor}}
\def\proc#1{\medbreak\noindent{\it #1}\hspace{1ex}\ignorespaces}
\def\ep{\noindent{\hfill $\Box$}}
\def\mb{\medbreak}
\def\ben{\begin{enumerate}}
\def\een{\end{enumerate}}
\def\nn{\notag\\}

% Modifications in v1.1:
% \century{NN} gives 20NN where NN<90 and 19NN otherwise;
%     used wherever a prefix `19' was used before.
% \@mycopyright added to \endsummary. 
% \author no longer \scshape\lowercase but mixed case.
% \twodigits now does the right thing by `S[1-9]' which are possible issue-nos.

%Modifications in v1.1a
%Copyright info added. 
%Update info added to title page runninghead
% bm.sty commented out
