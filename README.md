# logviewer
IntelliJ idea allows saving the output of console into log file. However, when you open the log file again in text editor, the hyperlinks in call stacks are missing.
A workaround for this issue would be:
1) Create a simple jar file that prints a text file line by line.
2) Run a configuration that calls this jar file and send the log file as parameter.
The result would be that all the text of the log file would be printed in a console, and the hyperlinks will be displayed.
Next step would be to write a simple plugin with an action called "Console" under "Open In" pop-menu. By launching the action, a temporary configuration that calls the jar file will be launched.
This simple IntelliJ plugin does that.
