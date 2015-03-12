# Introduction #

The project configuration for the shell plugin allows you to specify a program to execute along with arguments and also allows you to define environment variable to start the program with.

# Arguments #

Click the `Add` button to add as many arguments as you need.  If you need to execute multiple programs in succession, one way to accomplish this is to pass several programs into a shell such as bash:
```
/bin/sh -c "autoconf && ./configure && make"
```

You can accomplish the same as follows:

![http://farm2.static.flickr.com/1242/756065322_2f23993841_o.png](http://farm2.static.flickr.com/1242/756065322_2f23993841_o.png)

# Environment Variables #

Here you can define as many environment variables as needed in the form VAR=value.