package com.bhuvan_kumar.Presto.object;

/**
 * created by: Bk
 * date: 18.01.2018 20:53
 */

public interface Comparable
{
    boolean comparisonSupported();

    String getComparableName();

    long getComparableDate();

    long getComparableSize();
}
