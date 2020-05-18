# Command Line Generator

**Why did I build this?**

I started to see a pattern in some common work I was doing often so I decided to create this to
help me complete that work more efficiently.

**What is it?**

A command line interface for generating code.  Customized to the current project I am working
on but easy to change and fit other needs.

**WARNING**

This is my first time using Spring Shell so this was my learning project as well, so there are definitely
additional classes and files that helped me to learn as I built this.  For example the `learning` package.

Also there are some techniques that could be improved for example how some of the SQL statements are created.  Usually
I would use a prepared statement but since this is only a command line shell helper tool I was not worried about SQL 
Injection.

## How To Use

Ensure you have a datasource configured in `application.properties`.  The DatabaseOperations implementation is 
determined based on the `spring.datasource.url` property.

Also make sure to configure your `model`, `repository` and `controller-tests` properties.

Run the application main class.  Then use the basic terminal that is provided.  Use the help
command to see the commands that are available.  The `generate-code` command is the generator
all other commands are commands I used to learn the framework or were given by default.

In development but definitely usable already.

**Does**

- Generate a model

**Coming soon**

- Generate a repository
- Generate a test controller

## Additional Links

Some resources that helped me build this solution.

- [Spring Shell Docs](https://spring.io/projects/spring-shell)

## TODO

- Generate repository, tests construct
- If the model exists allow the user to rename? overwrite?