wget https://github.com/facebook/ktfmt/releases/download/v0.54/ktfmt-0.54-jar-with-dependencies.jar -O $HOME/ktfmt.jar
touch $HOME/.bashrc
echo """
function ktfmt {
  file=\"\$HOME/ktfmt.jar\"
  if test -f \"\$file\" ; then
    if [ -n \"\$1\" ]; then
      java -jar \$file --kotlinlang-style \"\$1\"
    else
      echo \"Usage: formatkt <path-to-file-or-directory>\"
    fi
  else
    echo \"Unable to format\"
  fi
}

export -f ktfmt
""" >> $HOME/.bashrc

source $HOME/.bashrc