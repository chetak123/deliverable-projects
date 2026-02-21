package com.library.model;

import java.util.*;

/**
 * Represents a library branch.
 * Supports multi-branch library systems.
 */
public class Branch {
    private final String branchId;
    private String name;
    private String location;
    private final Map<String, Book> inventory;

    public Branch(String name, String location) {
        this.branchId = UUID.randomUUID().toString();
        this.name = name;
        this.location = location;
        this.inventory = new HashMap<>();
    }

    // Getters
    public String getBranchId() {
        return branchId;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public Map<String, Book> getInventory() {
        return Collections.unmodifiableMap(inventory);
    }

    // Setters
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Branch name cannot be null or empty");
        }
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Business methods
    public void addBook(Book book) {
        inventory.put(book.getIsbn(), book);
        book.setCurrentBranchId(this.branchId);
    }

    public void removeBook(String isbn) {
        Book book = inventory.remove(isbn);
        if (book != null) {
            book.setCurrentBranchId(null);
        }
    }

    public Book getBook(String isbn) {
        return inventory.get(isbn);
    }

    public boolean hasBook(String isbn) {
        return inventory.containsKey(isbn);
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(inventory.values());
    }

    public List<Book> getAvailableBooks() {
        List<Book> available = new ArrayList<>();
        for (Book book : inventory.values()) {
            if (book.getStatus() == BookStatus.AVAILABLE) {
                available.add(book);
            }
        }
        return available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return Objects.equals(branchId, branch.branchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(branchId);
    }

    @Override
    public String toString() {
        return String.format("Branch{id='%s', name='%s', location='%s', books=%d}",
                branchId, name, location, inventory.size());
    }
}

