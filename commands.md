# Commands

## Key
| Symbol     | Meaning                    |
| ---------- | -------------------------- |
| (Argument) | This argument is optional. |

## services-demo
| Commands     | Arguments | Description              |
| ------------ | --------- | ------------------------ |
| dependsOnAll | <none>    | I depend on all services |

## utility
| Commands         | Arguments          | Description                                              |
| ---------------- | ------------------ | -------------------------------------------------------- |
| add              | Integer, Integer   | Add two numbers together                                 |
| conversationtest | <none>             | Test the implementation of the ConversationDSL           |
| echo             | Text               | No Description Provider                                  |
| guildowner       | <none>             | Provide info about the guild you executed the command in |
| guildsize        | <none>             | Display how many members are in a guild                  |
| help             | (Word)             | Display a help menu                                      |
| optionalAdd      | Integer, (Integer) | Add two numbers together                                 |
| optionalInput    | (Text)             | Optionally input some text                               |

## uncategorized
| Commands    | Arguments | Description                                                                                                                                                                                   |
| ----------- | --------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| data-save   | <none>    | This command tests the save command                                                                                                                                                           |
| data-see    | <none>    | This command depends on the data object above, which is automatically loaded from the designated path.if it does not exist at the designated path, it is created using the default arguments. |
| someCommand | <none>    | No Description Provider                                                                                                                                                                       |

## info
| Commands | Arguments | Description                            |
| -------- | --------- | -------------------------------------- |
| version  | <none>    | A command which will show the verison. |

