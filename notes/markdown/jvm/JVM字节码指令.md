- 栈帧(Stack Frame)的局部变量表中的最小单位为slot（变量槽）
- 64位操作系统中，slot占64位。所有数据类型都占1个slot
- 大多数对于boolean、byte、short和char类型数据的操作，都使用相应的int类型作为运算类型。
- 存储数据的操作数栈和局部变量表主要就是由加载和存储指令进行操作
- 

- const系列 把简单的数值类型送到栈顶
```
指令码    助记符                            说明

0x02         iconst_m1                   将int型(-1)推送至栈顶

0x03         iconst_0                      将int型(0)推送至栈顶

0x04         iconst_1                      将int型(1)推送至栈顶

0x05         iconst_2                      将int型(2)推送至栈顶

0x06         iconst_3                      将int型(3)推送至栈顶

0x07         iconst_4                      将int型(4)推送至栈顶

0x08         iconst_5                      将int型(5)推送至栈顶

0x09         lconst_0                      将long型(0)推送至栈顶

0x0a         lconst_1                      将long型(1)推送至栈顶

0x0b         fconst_0                      将float型(0)推送至栈顶

0x0c         fconst_1                      将float型(1)推送至栈顶

0x0d         fconst_2                      将float型(2)推送至栈顶

0x0e         dconst_0                     将double型(0)推送至栈顶

0x0f          dconst_1                     将double型(1)推送至栈顶
```
- push系列
- 把一个整形数字（长度比较小）送到到栈顶；只能操作一定范围内的整形数值，超出该范围的使用将使用ldc命令系列
```
指令码    助记符                            说明

0x10          bipush    将单字节的常量值(-128~127)推送至栈顶

0x11           sipush    将一个短整型常量值(-32768~32767)推送至栈顶
```
- ldc系列
- 数值常量或String常量值从常量池中推送至栈顶。该命令后面需要给一个表示常量在常量池中位置(编号)的参数
- 对于const系列命令和push系列命令操作范围之外的数值类型常量，都放在常量池中.
```
指令码    助记符                               说明

0x12            ldc                 将int, float或String型常量值从常量池中推送至栈顶

0x13          ldc_w               将int, float或String型常量值从常量池中推送至栈顶（宽索引）

0x14          ldc2_w             将long或double型常量值从常量池中推送至栈顶（宽索引）
```
- load系列 把本地变量的送到栈顶
- 对于非静态函数，第一变量是this,即其对于的操作是aload_0.
- 还有函数传入参数也算本地变量，在进行编号时，它是先于函数体的本地变量的。
```
指令码    助记符                                        说明

0x15          iload                          将指定的int型本地变量推送至栈顶

0x16          lload                          将指定的long型本地变量推送至栈顶

0x17          fload                          将指定的float型本地变量推送至栈顶

0x18          dload                         将指定的double型本地变量推送至栈顶

0x19          aload                         将指定的引用类型本地变量推送至栈顶

0x1a          iload_0                      将第1个槽位的本地变量数据按int型推送至栈顶

0x1b          iload_1                      将第2个槽位的本地变量数据按int型推送至栈顶

0x1c          iload_2                      将第3个槽位的本地变量数据按int型推送至栈顶

0x1d          iload_3                      将第4个槽位的本地变量数据按int型推送至栈顶

0x1e          lload_0                      将第1个槽位的本地变量数据按long型推送至栈顶

0x1f           lload_1                      将第2个槽位的本地变量数据按long型推送至栈顶

0x20          lload_2                      将第3个槽位的本地变量数据按long型推送至栈顶

0x21          lload_3                      将第4个槽位的本地变量数据按long型推送至栈顶

0x22          fload_0                     将第1个槽位的本地变量数据按float型推送至栈顶

0x23          fload_1                     将第2个槽位的本地变量数据按float型推送至栈顶

0x24          fload_2                     将第3个槽位的本地变量数据按float型推送至栈顶

0x25          fload_3                     将第4个槽位的本地变量数据按float型推送至栈顶

0x26         dload_0                     将第1个槽位的本地变量数据按double型推送至栈顶

0x27         dload_1                     将第2个槽位的本地变量数据按double型推送至栈顶

0x28         dload_2                     将第3个槽位的本地变量数据按double型推送至栈顶

0x29         dload_3                     将第1个槽位的本地变量数据按double型推送至栈顶

0x2a         aload_0                     将第2个槽位的本地变量数据按引用型推送至栈顶

0x2b         aload_1                     将第2个槽位的本地变量数据按引用型推送至栈顶

0x2c         aload_2                     将第3个槽位的本地变量数据按引用型推送至栈顶

0x2d         aload_3                     将第4个槽位的本地变量数据按引用型推送至栈顶
```

- load系列 把数组的某项送到栈顶
- 该命令根据栈里内容来确定对哪个数组的哪项进行操作。
```
指令码    助记符                               说明

0x2e         iaload                     将int型数组指定索引的值推送至栈顶

0x2f          laload                     将long型数组指定索引的值推送至栈顶

0x30         faload                     将float型数组指定索引的值推送至栈顶

0x31        daload                     将double型数组指定索引的值推送至栈顶

0x32        aaload                     将引用型数组指定索引的值推送至栈顶

0x33        baload                     将boolean或byte型数组指定索引的值推送至栈顶

0x34        caload                     将char型数组指定索引的值推送至栈顶

0x35        saload                     将short型数组指定索引的值推送至栈顶
```

- store系列
- 把栈顶的值存入本地变量
- 对本地变量所进行的编号，是对所有类型的本地变量进行的（并不按照类型分类）。
- 对于非静态函数，第一变量是this,它是只读的.
- 还有函数传入参数也算本地变量，在进行编号时，它是先于函数体的本地变量的。
```
指令码    助记符                               说明

0x36         istore                    将栈顶int型数值存入指定本地变量

0x37         lstore                    将栈顶long型数值存入指定本地变量

0x38         fstore                    将栈顶float型数值存入指定本地变量

0x39         dstore                   将栈顶double型数值存入指定本地变量

0x3a         astore                   将栈顶引用型数值存入指定本地变量

0x3b         istore_0                将栈顶int型数值存入第一个本地变量

0x3c         istore_1                将栈顶int型数值存入第二个本地变量

0x3d         istore_2                将栈顶int型数值存入第三个本地变量

0x3e         istore_3                将栈顶int型数值存入第四个本地变量

0x3f          lstore_0                将栈顶long型数值存入第一个本地变量

0x40         lstore_1                将栈顶long型数值存入第二个本地变量

0x41         lstore_2                将栈顶long型数值存入第三个本地变量

0x42         lstore_3                将栈顶long型数值存入第四个本地变量

0x43         fstore_0                将栈顶float型数值存入第一个本地变量

0x44         fstore_1                将栈顶float型数值存入第二个本地变量

0x45         fstore_2                将栈顶float型数值存入第三个本地变量

0x46         fstore_3                将栈顶float型数值存入第四个本地变量

0x47         dstore_0               将栈顶double型数值存入第一个本地变量

0x48         dstore_1               将栈顶double型数值存入第二个本地变量

0x49         dstore_2               将栈顶double型数值存入第三个本地变量

0x4a         dstore_3               将栈顶double型数值存入第四个本地变量

0x4b         astore_0               将栈顶引用型数值存入第一个本地变量

0x4c         astore_1               将栈顶引用型数值存入第二个本地变量

0x4d        astore_2                将栈顶引用型数值存入第三个本地变量

0x4e        astore_3                将栈顶引用型数值存入第四个本地变量
```
- store系列
- 把栈顶项的值存到数组里
```
指令码    助记符                                   说明

0x4f         iastore               将栈顶int型数值存入指定数组的指定索引位置

0x50        lastore               将栈顶long型数值存入指定数组的指定索引位置

0x51        fastore               将栈顶float型数值存入指定数组的指定索引位置

0x52        dastore              将栈顶double型数值存入指定数组的指定索引位置

0x53        aastore              将栈顶引用型数值存入指定数组的指定索引位置

0x54        bastore              将栈顶boolean或byte型数值存入指定数组的指定索引位置

0x55        castore              将栈顶char型数值存入指定数组的指定索引位置

0x56        sastore              将栈顶short型数值存入指定数组的指定索引位置
```
- 该系列命令用于对栈顶元素行数学操作，和对数值进行移位操作。移位操作的操作数和要移位的数都是从栈里取得。

```
指令码     助记符                                        说明

0x5f             swap               将栈最顶端的两个数值互换(数值不能是long或double类型的)

0x60            iadd                将栈顶两int型数值相加并将结果压入栈顶

0x61            ladd                将栈顶两long型数值相加并将结果压入栈顶

0x62            fadd               将栈顶两float型数值相加并将结果压入栈顶

0x63            dadd              将栈顶两double型数值相加并将结果压入栈顶

0x64            isub               将栈顶两int型数值相减并将结果压入栈顶

0x65            lsub              将栈顶两long型数值相减并将结果压入栈顶

0x66            fsub              将栈顶两float型数值相减并将结果压入栈顶

0x67            dsub             将栈顶两double型数值相减并将结果压入栈顶

0x68            imul              将栈顶两int型数值相乘并将结果压入栈顶

0x69            lmul              将栈顶两long型数值相乘并将结果压入栈顶

0x6a            fmul              将栈顶两float型数值相乘并将结果压入栈顶

0x6b            dmul             将栈顶两double型数值相乘并将结果压入栈顶

0x6c            idiv               将栈顶两int型数值相除并将结果压入栈顶

0x6d            ldiv               将栈顶两long型数值相除并将结果压入栈顶

0x6e            fdiv               将栈顶两float型数值相除并将结果压入栈顶

0x6f            ddiv               将栈顶两double型数值相除并将结果压入栈顶

0x70           irem               将栈顶两int型数值作取模运算并将结果压入栈顶

0x71           lrem               将栈顶两long型数值作取模运算并将结果压入栈顶

0x72           frem               将栈顶两float型数值作取模运算并将结果压入栈顶

0x73           drem              将栈顶两double型数值作取模运算并将结果压入栈顶

0x74            ineg              将栈顶int型数值取负并将结果压入栈顶

0x75            lneg              将栈顶long型数值取负并将结果压入栈顶

0x76           fneg              将栈顶float型数值取负并将结果压入栈顶

0x77           dneg             将栈顶double型数值取负并将结果压入栈顶

0x78            ishl               将int型数值左移位指定位数并将结果压入栈顶

0x79            lshl               将long型数值左移位指定位数并将结果压入栈顶

0x7a            ishr               将int型数值右（符号）移位指定位数并将结果压入栈顶

0x7b            lshr               将long型数值右（符号）移位指定位数并将结果压入栈顶

0x7c            iushr             将int型数值右（无符号）移位指定位数并将结果压入栈顶

0x7d           lushr              将long型数值右（无符号）移位指定位数并将结果压入栈顶

0x7e           iand               将栈顶两int型数值作“按位与”并将结果压入栈顶

0x7f            land               将栈顶两long型数值作“按位与”并将结果压入栈顶

0x80            ior                 将栈顶两int型数值作“按位或”并将结果压入栈顶

0x81            lor                 将栈顶两long型数值作“按位或”并将结果压入栈顶

0x82            ixor               将栈顶两int型数值作“按位异或”并将结果压入栈顶

0x83            lxor               将栈顶两long型数值作“按位异或”并将结果压入栈顶
```


- 运算指令
- 1、运算或算术指令用于对两个操作数栈上的值进行某种特定运算，并把结果重新存入到操作栈顶。

- 3、无论是哪种算术指令，都使用Java虚拟机的数据类型，由于没有直接支持byte、short、char和boolean类型的算术指令，使用操作int类型的指令代替
```
加法指令：iadd、ladd、fadd、dadd。
减法指令：isub、lsub、fsub、dsub。
乘法指令：imul、lmul、fmul、dmul。
除法指令：idiv、ldiv、fdiv、ddiv。
求余指令：irem、lrem、frem、drem。
取反指令：ineg、lneg、fneg、dneg。
位移指令：ishl、ishr、iushr、lshl、lshr、lushr。
按位或指令：ior、lor。
按位与指令：iand、land。
按位异或指令：ixor、lxor。
局部变量自增指令：iinc。
比较指令：dcmpg、dcmpl、fcmpg、fcmpl、lcmp。
```
- 对象创建与访问指令
```

创建类实例的指令：new。
创建数组的指令：newarray、anewarray、multianewarray。
访问类字段（static字段，或者称为类变量）和实例字段（非static字段，或者称为实例变量）的指令：getfield、putfield、getstatic、putstatic。
把一个数组元素加载到操作数栈的指令：baload、caload、saload、iaload、laload、faload、daload、aaload。
将一个操作数栈的值存储到数组元素中的指令：bastore、castore、sastore、iastore、fastore、dastore、aastore。
取数组长度的指令：arraylength。
检查类实例类型的指令：instanceof、checkcast
```
- 方法调用和返回指令
```
invokevirtual 指令用于调用对象的实例方法，根据对象的实际类型进行分派（虚方法分派），这也是Java语言中最常见的方法分派方式。
invokeinterface 指令用于调用接口方法，它会在运行时搜索一个实现了这个接口方法的对象，找出适合的方法进行调用。
invokespecial 指令用于调用一些需要特殊处理的实例方法，包括实例初始化（＜init＞）方法、私有方法和父类方法。
invokestatic  调用静态方法（static方法）。
invokedynamic 指令用于在运行时动态解析出调用点限定符所引用的方法，并执行该方法，前面4条调用指令的分派逻辑都固化在Java虚拟机内部，而invokedynamic指令的分派逻辑是由用户所设定的引导方法决定的。

方法调用指令与数据类型无关，而方法返回指令是根据返回值的类型区分的，包括ireturn（当返回值是boolean、byte、char、short和int类型时使用）、lreturn、freturn、dreturn和areturn，另外还有一条return指令供声明为void的方法、实例初始化方法以及类和接口的类初始化方法使用。
```

