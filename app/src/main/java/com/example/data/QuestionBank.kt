package com.example.data

data class Question(
    val id: String,
    val board: String, // "CBSE" or "ICSE" or "UP Board" or "Bihar Board" or "State Board"
    val classLevel: Int, // 5 to 12
    val subject: String, // "Mathematics", "Science", "Physics", "Chemistry", "Social Science", "English"
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int, // 0 for A, 1 for B, 2 for C, 3 for D
    val offlineExplanation: String
)

object QuestionBank {
    val questions = listOf(
        // --- CLASS 5 ---
        Question(
            id = "c5_m1",
            board = "CBSE",
            classLevel = 5,
            subject = "Mathematics",
            questionText = "If a school has 480 students and they are divided equally into 12 sections, how many students will be there in each section?",
            options = listOf("30 students", "40 students", "45 students", "50 students"),
            correctAnswerIndex = 1,
            offlineExplanation = "To find the number of students in each section, divide the total number of students by the number of sections. So, 480 / 12 = 40 students. Each section has 40 students."
        ),
        Question(
            id = "c5_s1",
            board = "CBSE",
            classLevel = 5,
            subject = "Science",
            questionText = "Which part of the seed stores food for the baby plant?",
            options = listOf("Seed coat", "Cotyledon", "Radicle", "Plumule"),
            correctAnswerIndex = 1,
            offlineExplanation = "The cotyledons (also called seed leaves) are the parts of a seed that store food for the developing baby plant (embryo) until it can grow green leaves and make its own food."
        ),
        Question(
            id = "c5_e1",
            board = "ICSE",
            classLevel = 5,
            subject = "English",
            questionText = "Identify the adjective in the sentence: 'The courageous young soldier saved the small puppy.'",
            options = listOf("soldier", "saved", "courageous", "puppy"),
            correctAnswerIndex = 2,
            offlineExplanation = "An adjective describes a noun. In this sentence, 'courageous' and 'young' describe the noun 'soldier', and 'small' describes 'puppy'. Out of the choices, 'courageous' is the adjective."
        ),

        // --- CLASS 6 ---
        Question(
            id = "c6_m1",
            board = "CBSE",
            classLevel = 6,
            subject = "Mathematics",
            questionText = "Which of the following numbers is a prime number?",
            options = listOf("21", "27", "29", "33"),
            correctAnswerIndex = 2,
            offlineExplanation = "A prime number is a number greater than 1 that has exactly two factors: 1 and itself. 29 can only be divided by 1 and 29. 21 (3x7), 27 (3x9), and 33 (3x11) are composite numbers."
        ),
        Question(
            id = "c6_s1",
            board = "CBSE",
            classLevel = 6,
            subject = "Science",
            questionText = "Scurvy is caused due to the deficiency of which vitamin?",
            options = listOf("Vitamin A", "Vitamin B1", "Vitamin C", "Vitamin D"),
            correctAnswerIndex = 2,
            offlineExplanation = "Scurvy is a disease caused by severe lack of Vitamin C (ascorbic acid) in the diet. It leads to bleeding gums, weak bones, and joint pain. Citrus fruits like oranges and lemons are rich in Vitamin C."
        ),

        // --- CLASS 7 ---
        Question(
            id = "c7_m1",
            board = "UP Board",
            classLevel = 7,
            subject = "Mathematics",
            questionText = "Solve the equation: 3x - 7 = 8. What is the value of x?",
            options = listOf("x = 3", "x = 5", "x = 15", "x = 4"),
            correctAnswerIndex = 1,
            offlineExplanation = "Let's solve step by step:\n1. Add 7 to both sides: 3x = 8 + 7 => 3x = 15.\n2. Divide by 3: x = 15 / 3 => x = 5. Therefore, the answer is x = 5."
        ),
        Question(
            id = "c7_s1",
            board = "CBSE",
            classLevel = 7,
            subject = "Science",
            questionText = "What is the mode of nutrition in green plants called?",
            options = listOf("Heterotrophic", "Autotrophic", "Saprophytic", "Parasitic"),
            correctAnswerIndex = 1,
            offlineExplanation = "Green plants prepare their own food from simple inorganic substances using solar energy (photosynthesis). Therefore, their mode of nutrition is called Autotrophic (Auto = self, trophos = nourishment)."
        ),

        // --- CLASS 8 ---
        Question(
            id = "c8_m1",
            board = "CBSE",
            classLevel = 8,
            subject = "Mathematics",
            questionText = "What is the sum of the interior angles of a regular pentagon?",
            options = listOf("360°", "540°", "720°", "180°"),
            correctAnswerIndex = 1,
            offlineExplanation = "The formula for the sum of interior angles of an n-sided polygon is (n - 2) * 180°. For a pentagon, n = 5. So, (5 - 2) * 180° = 3 * 180° = 540°."
        ),
        Question(
            id = "c8_s1",
            board = "CBSE",
            classLevel = 8,
            subject = "Science",
            questionText = "Which non-metal is extremely reactive and stored under water to prevent contact with air?",
            options = listOf("Sodium", "Phosphorus", "Sulfur", "Carbon"),
            correctAnswerIndex = 1,
            offlineExplanation = "Phosphorus (specifically white phosphorus) is a very reactive non-metal. It catches fire if exposed to air. To prevent contact with atmospheric oxygen, it is stored under water. (Sodium is stored under kerosene because it reacts with water)."
        ),

        // --- CLASS 9 ---
        Question(
            id = "c9_m1",
            board = "Bihar Board",
            classLevel = 9,
            subject = "Mathematics",
            questionText = "Find the area of an equilateral triangle whose side is 4 cm. (Formula: (sqrt(3)/4) * side^2)",
            options = listOf("4√3 cm²", "8√3 cm²", "16√3 cm²", "2√3 cm²"),
            correctAnswerIndex = 0,
            offlineExplanation = "Area of equilateral triangle = (√3 / 4) * side².\nGiven side = 4 cm.\nArea = (√3 / 4) * 4² = (√3 / 4) * 16 = 4√3 cm²."
        ),
        Question(
            id = "c9_s1",
            board = "CBSE",
            classLevel = 9,
            subject = "Science",
            questionText = "Which organelle is known as the 'Powerhouse of the Cell'?",
            options = listOf("Lysosome", "Mitochondria", "Golgi Apparatus", "Plastid"),
            correctAnswerIndex = 1,
            offlineExplanation = "Mitochondria are known as the powerhouse of the cell because they generate most of the cell's supply of adenosine triphosphate (ATP), which is used as a source of chemical energy."
        ),

        // --- CLASS 10 ---
        Question(
            id = "c10_m1",
            board = "CBSE",
            classLevel = 10,
            subject = "Mathematics",
            questionText = "If the discriminant of a quadratic equation ax² + bx + c = 0 is greater than zero (D > 0), what is the nature of its roots?",
            options = listOf("No real roots", "Two equal real roots", "Two distinct real roots", "Imaginary roots"),
            correctAnswerIndex = 2,
            offlineExplanation = "The discriminant D = b² - 4ac determines the nature of roots:\n- If D > 0, the equation has two distinct real roots.\n- If D = 0, it has two equal real roots.\n- If D < 0, it has no real roots (roots are imaginary)."
        ),
        Question(
            id = "c10_s1",
            board = "CBSE",
            classLevel = 10,
            subject = "Science",
            questionText = "What is the product formed when quicklime (CaO) reacts vigorously with water?",
            options = listOf("Calcium Carbonate [CaCO3]", "Slaked Lime [Ca(OH)2]", "Calcium Oxide [CaO]", "Calcium Chloride [CaCl2]"),
            correctAnswerIndex = 1,
            offlineExplanation = "When quicklime (Calcium Oxide, CaO) reacts with water, it forms Slaked Lime (Calcium Hydroxide, Ca(OH)2) in a highly exothermic reaction (releases a lot of heat): CaO(s) + H2O(l) -> Ca(OH)2(aq) + heat."
        ),
        Question(
            id = "c10_ss1",
            board = "CBSE",
            classLevel = 10,
            subject = "Social Science",
            questionText = "Who was responsible for the unification of Germany?",
            options = listOf("Giuseppe Garibaldi", "Otto von Bismarck", "Napoleon Bonaparte", "Kaiser William II"),
            correctAnswerIndex = 1,
            offlineExplanation = "Otto von Bismarck, the Chief Minister of Prussia, was the main architect of the unification of Germany. He followed the policy of 'Blood and Iron' to achieve this through three wars over seven years."
        ),

        // --- CLASS 11 ---
        Question(
            id = "c11_p1",
            board = "CBSE",
            classLevel = 11,
            subject = "Physics",
            questionText = "The work done by a conservative force along a closed path is always:",
            options = listOf("Positive", "Negative", "Zero", "Dependent on the speed"),
            correctAnswerIndex = 2,
            offlineExplanation = "By definition, a force is conservative if the work done by it in moving a particle between two points is independent of the path taken. Consequently, the net work done by a conservative force (like gravity or electrostatic force) over any closed loop is exactly zero."
        ),
        Question(
            id = "c11_c1",
            board = "ICSE",
            classLevel = 11,
            subject = "Chemistry",
            questionText = "Which quantum number describes the orientation of an orbital in space?",
            options = listOf("Principal quantum number (n)", "Azimuthal quantum number (l)", "Magnetic quantum number (ml)", "Spin quantum number (ms)"),
            correctAnswerIndex = 2,
            offlineExplanation = "The magnetic quantum number (ml) specifies the behavior of an electron in a magnetic field and describes the spatial orientation of the orbital in space relative to a set of coordinate axes."
        ),

        // --- CLASS 12 ---
        Question(
            id = "c12_m1",
            board = "CBSE",
            classLevel = 12,
            subject = "Mathematics",
            questionText = "What is the value of the integral of 1/x dx from 1 to e?",
            options = listOf("0", "e", "1", "ln(2)"),
            correctAnswerIndex = 2,
            offlineExplanation = "The integral of 1/x dx is ln|x| + C.\nEvaluating from 1 to e gives: [ln(e) - ln(1)] = [1 - 0] = 1."
        ),
        Question(
            id = "c12_p1",
            board = "CBSE",
            classLevel = 12,
            subject = "Physics",
            questionText = "According to Lenz's law of electromagnetic induction, the direction of induced current is such that it:",
            options = listOf("Supports the change in magnetic flux", "Opposes the change in magnetic flux", "Is always clockwise", "Is independent of the magnetic flux"),
            correctAnswerIndex = 1,
            offlineExplanation = "Lenz's Law states that the direction of the induced current in a conductor will be such that it creates a magnetic field that opposes the change in magnetic flux that produced it. This is a direct consequence of the conservation of energy."
        ),
        Question(
            id = "c12_c1",
            board = "CBSE",
            classLevel = 12,
            subject = "Chemistry",
            questionText = "Which of the following organic compounds will give a positive silver mirror test (Tollens' Test)?",
            options = listOf("Acetone", "Propanal", "Ethanol", "Ethyl Acetate"),
            correctAnswerIndex = 1,
            offlineExplanation = "Tollens' reagent is a mild oxidizing agent. Only aldehydes (like Propanal) are oxidized by Tollens' reagent to form a carboxylic acid and reduce Ag+ ions to metallic silver, forming a mirror. Ketones (like Acetone) do not give this test."
        )
    )

    // Helper to get questions based on user's profile
    fun getFilteredQuestions(board: String, classLevel: Int, subject: String? = null): List<Question> {
        val classFiltered = questions.filter { it.classLevel == classLevel }
        val subjectFiltered = if (subject != null) {
            classFiltered.filter { it.subject.equals(subject, ignoreCase = true) }
        } else {
            classFiltered
        }
        
        // If empty, return any questions for this class level
        if (subjectFiltered.isEmpty()) {
            return classFiltered.ifEmpty { questions.filter { it.classLevel == 10 } }
        }
        return subjectFiltered
    }
}
