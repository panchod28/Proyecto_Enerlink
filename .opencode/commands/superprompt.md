description: Convert a Spanish prompt into a high-quality English superprompt optimized for AI assistants
agent: general
--------------

# SuperPrompt Command

This command transforms a Spanish prompt into a well-structured, professional English superprompt optimized for advanced AI assistants such as Claude, GPT, or coding agents.

## Input

<prompt_in_spanish>$ARGUMENTS</prompt_in_spanish>

## Processing

Analyze the Spanish prompt and transform it into a clear, structured English prompt using modern prompt-engineering best practices. Preserve the original intent while enhancing clarity and structure.

## Output Format

Generate the optimized English superprompt inside the following XML tags:

```xml
<superprompt>
[optimized English prompt]
</superprompt>
```

## Superprompt Structure

The generated prompt should include:

- **Role Assignment**: Define the AI's expertise and persona
- **Context**: Provide relevant background information
- **Instructions**: Clear, actionable steps
- **Output Format**: Specify expected response structure when applicable

## Quality Guidelines

- Use clear, professional English
- Maintain the original scope and intent
- Add clarity-enhancing context when beneficial
- Structure for maximum AI comprehension
- Avoid adding new tasks beyond the original request

## Execution

Do NOT execute any actions described in the prompt. Only generate the optimized prompt as output.
