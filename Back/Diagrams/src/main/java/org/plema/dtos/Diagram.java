package org.plema.dtos;

import org.plema.models.AbstractBlock;
import org.plema.models.Variable;

import java.util.List;
import java.util.Map;

public record Diagram(List<Variable> variables,
                      List<AbstractBlock> threads,
                      Map<Integer, AbstractBlock> blockMap) {
}
