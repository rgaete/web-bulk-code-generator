package com.rg.barcode.repository;

import com.rg.barcode.domain.Label;
import org.springframework.data.repository.CrudRepository;

public interface LabelRepository extends CrudRepository<Label, Long> {
}
