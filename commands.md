# Commands

## Key
| Symbol     | Meaning                    |
| ---------- | -------------------------- |
| (Argument) | This argument is optional. |

## Basic
| Commands   | Arguments        | Description               |
| ---------- | ---------------- | ------------------------- |
| Add        | Integer, Integer | Add two numbers together. |
| Echo       | Text             | Echo some text back.      |
| Hello      | <none>           | Display a simple message. |
| Version, V | <none>           | Display the version.      |

## Conversation Demo
| Commands | Arguments | Description                                         |
| -------- | --------- | --------------------------------------------------- |
| Private  | (User)    | Start a conversation with the user in DM's.         |
| Public   | (User)    | Start a conversation with the user in this channel. |

## DSL Demo
| Commands | Arguments | Description               |
| -------- | --------- | ------------------------- |
| Embed    | <none>    | Display an example embed. |
| Menu     | <none>    | Display an example menu.  |

## Data Demo
| Commands | Arguments | Description                                            |
| -------- | --------- | ------------------------------------------------------ |
| DataSave | <none>    | This command lets you modify a Data object's contents. |
| DataSee  | <none>    | This command lets you view a Data object's contents.   |

## Optional
| Commands    | Arguments          | Description                          |
| ----------- | ------------------ | ------------------------------------ |
| Guild       | (Guild)            | Display the current guild name.      |
| Null        | (Any)              | Display the first element in a list. |
| OptionalAdd | Integer, (Integer) | Add two numbers together.            |
| User        | (User)             | Display this user's full tag.        |

## Services Demo
| Commands  | Arguments | Description              |
| --------- | --------- | ------------------------ |
| Injection | <none>    | I depend on all services |

## Special
| Commands | Arguments      | Description                        |
| -------- | -------------- | ---------------------------------- |
| Either   | Integer \| Any | Enter a number or any input.       |
| Eval     | Script         | Evaluate a Kotlin expression.      |
| File     | File           | Input a file and display its name. |
| Sum      | Integer...     | Add a list of numbers together.    |

## Utility
| Commands | Arguments | Description          |
| -------- | --------- | -------------------- |
| Help     | (Command) | Display a help menu. |

