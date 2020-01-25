;;; Major mode for FL

(require 'quail)

(defvar fl-eval-command "fleval")

(defun fl-input-setup ()
  (with-temp-buffer
    (quail-define-package
     "FL" "UTF-8" "FL" t
     nil nil nil nil nil nil t))
  (quail-defrule "->" "→" "FL" t)
  (quail-defrule "<-" "←" "FL" t)
  (quail-defrule "<-|" "↤" "FL" t)
  (quail-defrule "<-\\" "↤" "FL" t)
  (quail-defrule "|->" "↦" "FL" t)
  (quail-defrule "\\->" "↦" "FL" t)
  (quail-defrule "/\\" "∧" "FL" t)
  (quail-defrule "\\/" "∨" "FL" t)
  (quail-defrule "|=>" "⤇" "FL" t)
  (quail-defrule "\\=>" "⤇" "FL" t)
  (quail-defrule "<=|" "⤆" "FL" t)
  (quail-defrule "<=\\" "⤆" "FL" t)
  (quail-defrule "=f=>" "⇒" "FL" t)
  (quail-defrule "Not" "¬" "FL" t)
  (quail-defrule "@@" "∘" "FL" t)
  (quail-defrule "\\@" "∘" "FL" t)
  (quail-defrule "\\l" "λ" "FL" t)
  (quail-defrule "\\a" "α" "FL" t)
  (quail-defrule "\\D" "Δ" "FL" t)
  (quail-defrule "[|" "⟦" "FL" t)
  (quail-defrule "\\[" "⟦" "FL" t)
  (quail-defrule "|]" "⟧" "FL" t)
  (quail-defrule "\\]" "⟧" "FL" t)
  (quail-defrule "==" "≡" "FL" t)
  (quail-defrule "//" "÷" "FL" t))

(fl-input-setup)

(defconst fl-builtins
  '("isint" "isreal" "isnum" "ispos" "isneg" "iszero"
    "isatom" "isbool" "ischar" "isutype" "isfunc" "isobj"
    "isnull" "ispair" "isseq" "isstring" "ff" "isval" "tt"
    "and" "not" "or" "eq" "neq" "less" "greater" "lesseq"
    "greatereq" "lt" "gt" "le" "ge"
    "add" "sub" "mul" "div" "neg" "floor" "ceiling" "abs"
    "cons" "cond" "apply" "K" "lift" "C" "raise" "catch" "delay"
    "pcons" "seqof" "eqto" "lenis"
    "patcons" "pand" "or" "pcomp"
    "/l" "/r" "tree" "alpha" "merge"
    "al" "ar" "cat" "distl" "distr" "intston" "len"
    "reverse" "sel" "trans" "tl" "tlr"
    "in" "out" "get" "put"
    "id" "delta" "signal"))

(defconst fl-keywords
  '("asn" "def" "exdef" "export" "false" "hide"
    "lambda" "lib" "nrdef" "pat" "PF" "rec"
    "sig" "true" "type" "union" "uses" "where"))

(defconst fl-function-name-regex
  "\\([a-zA-Z/$%#_?^][a-zA-Z0-9/$%#_?^]*\\)")

(defun fl-make-regex (names)
  (concat "\\<\\("
          (mapconcat (lambda (x) x) names "\\|")
          "\\)\\>"))

(defconst fl-font-lock-keywords
  (list
   (list (fl-make-regex fl-keywords)
         1 'font-lock-keyword-face)
   (list (fl-make-regex fl-builtins)
         1 'font-lock-builtin-face)
   (list (concat "\\<\\(def\\|nrdef\\|type\\)\\> +"
                 fl-function-name-regex)
         2 'font-lock-function-name-face)
   (list fl-function-name-regex
         1 'font-lock-function-name-face)
   (list "\\<\\([sr][0-9]+\\)\\>" 1 'font-lock-builtin-face)))

(defconst fl-mode-syntax-table
  (let ((st (make-syntax-table)))
    (modify-syntax-entry ?\_ "_" st)
    (modify-syntax-entry ?\/ "_" st)
    (modify-syntax-entry ?\$ "_" st)
    (modify-syntax-entry ?\% "_" st)
    (modify-syntax-entry ?\# "_" st)
    (modify-syntax-entry ?\? "_" st)
    (modify-syntax-entry ?\/  ". 14b" st)
    (modify-syntax-entry ?\*  ". 23"   st)
    st))

(defun fl-check-syntax ()
  (interactive)
  (compile (concat fl-eval-command " -c " (buffer-file-name))))

(defconst fl-mode-map
  (let ((map (make-sparse-keymap)))
    (define-key map "\C-c\C-c" 'fl-check-syntax)
    map)
  "Keymap used in FL mode.")

(defun fl-indent-line ())

(define-derived-mode fl-mode prog-mode "FL"
  "Major mode for editing FL code.\\<fl-mode-map>"
  (setq-local require-final-newline mode-require-final-newline)
  (use-local-map fl-mode-map)
  ;(setq-local indent-line-function 'fl-indent-line)
  (setq-local comment-start "/* ")
  (setq-local comment-end " */")
  (setq-local comment-start-skip "/\\*+ *\\|/ *")
  (setq-local comment-style 'multi-line)
  (setq-local indent-tabs-mode nil)
  (setq-local tab-width 4)
  (setq-local font-lock-defaults '(fl-font-lock-keywords))
  (set-syntax-table fl-mode-syntax-table)
  (make-local-variable 'default-input-method)
  (set-input-method "FL"))

(provide 'fl-mode)
