package com.github.eiriksgata.trpg.dice.operation.impl;

/**
 * author: create by Keith
 * version: v1.0
 * description: indi.eiriksgata.dice.operation.impl
 * date: 2020/11/9
 **/
public interface RollArrayCallback {

    void getResultData(int checkValue, int[] sortArr, int[] randomArr);
}
