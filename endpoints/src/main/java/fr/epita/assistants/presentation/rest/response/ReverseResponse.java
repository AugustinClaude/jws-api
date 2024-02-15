package fr.epita.assistants.presentation.rest.response;

public class ReverseResponse {
    public String original;
    public String reversed;

    public ReverseResponse(String original) {
        this.original = original;
        this.reversed = reverseOriginal();
    }

    private String reverseOriginal() {
        StringBuilder reversed = new StringBuilder();
        for (int i = original.length() - 1; i >= 0; i--)
            reversed.append(original.charAt(i));

        return reversed.toString();
    }
}
