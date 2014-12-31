package com.yiting.collection;
/*
 * write in 2014/5/20
 * @author yiting
 * 接口中不提供函数的接口 只是用于表明该类拥有某个功能，其一般会搭配instanceof  判断其他类是否实现了该接口
 * 该接口表明集合能够通过参数i 进行随机访问 如 list.get(i),这样的随机访问比iterator.next更见快速
 */

public interface RandomAccess {

}
