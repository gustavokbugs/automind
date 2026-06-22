package com.automind.util;

public class CpfUtils {

    private CpfUtils() {}

    public static String limpar(String cpf) {
        return cpf != null ? cpf.replaceAll("[^0-9]", "") : null;
    }

    public static String formatar(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf;
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." +
               cpf.substring(6, 9) + "-" + cpf.substring(9);
    }
}
